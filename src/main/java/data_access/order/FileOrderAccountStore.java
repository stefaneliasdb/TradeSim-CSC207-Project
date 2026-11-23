package data_access.order;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import entity.Order;
import entity.OrderType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * File-backed practice accounts. Cash, holdings and history are replaced together.
 * A stable sibling lock coordinates app instances; the account file itself is atomically replaced.
 * The team's eventual account DAO can instead implement OrderAccountGateway directly.
 */
public final class FileOrderAccountStore implements OrderAccountStore {
    private static final Map<Path, Object> PATH_LOCKS = new ConcurrentHashMap<>();
    private static final Type ACCOUNTS_TYPE = new TypeToken<Map<String, OrderAccountData>>() { }.getType();
    private static final Gson JSON = new GsonBuilder()
            .registerTypeAdapter(Order.class, (JsonSerializer<Order>) (order, type, context) -> {
                JsonObject value = new JsonObject();
                value.addProperty("orderID", order.getOrderID());
                value.addProperty("userID", order.getUserID());
                value.addProperty("symbol", order.getSymbol());
                value.addProperty("type", order.getType().name());
                value.addProperty("quantity", order.getQuantity());
                value.addProperty("executionPrice", order.getExecutionPrice());
                value.addProperty("executedAt", order.getExecutedAt().toString());
                return value;
            })
            .registerTypeAdapter(Order.class, (JsonDeserializer<Order>) (value, type, context) -> {
                JsonObject order = value.getAsJsonObject();
                return new Order(order.get("orderID").getAsString(), order.get("userID").getAsString(),
                        order.get("symbol").getAsString(), OrderType.valueOf(order.get("type").getAsString()),
                        order.get("quantity").getAsBigDecimal().intValueExact(),
                        order.get("executionPrice").getAsBigDecimal(),
                        Instant.parse(order.get("executedAt").getAsString()));
            })
            .setPrettyPrinting().create();

    private final Path file;
    private final Object processLock;

    public FileOrderAccountStore(Path file) {
        Objects.requireNonNull(file);
        try {
            Path absolute = file.toAbsolutePath().normalize();
            Files.createDirectories(absolute.getParent());
            this.file = absolute.getParent().toRealPath().resolve(absolute.getFileName());
            processLock = PATH_LOCKS.computeIfAbsent(this.file, ignored -> new Object());
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot open the practice account directory.", exception);
        }
    }

    @Override
    public Optional<OrderAccountData> find(String userID) {
        return locked(() -> Optional.ofNullable(read().get(userID)));
    }

    @Override
    public boolean create(String userID, OrderAccountData initial) {
        if (userID == null || userID.isBlank()) {
            throw new IllegalArgumentException("Account ID is required.");
        }
        Objects.requireNonNull(initial);
        return locked(() -> {
            Map<String, OrderAccountData> accounts = read();
            if (accounts.containsKey(userID)) {
                return false;
            }
            accounts.put(userID, initial);
            write(accounts);
            return true;
        });
    }

    @Override
    public boolean update(String userID, Function<OrderAccountData, Optional<OrderAccountData>> change) {
        Objects.requireNonNull(change);
        return locked(() -> {
            Map<String, OrderAccountData> accounts = read();
            OrderAccountData current = accounts.get(userID);
            if (current == null) {
                return false;
            }
            Optional<OrderAccountData> updated = Objects.requireNonNull(change.apply(current));
            if (updated.isEmpty()) {
                return false;
            }
            accounts.put(userID, updated.get());
            write(accounts);
            return true;
        });
    }

    private Map<String, OrderAccountData> read() {
        if (Files.notExists(file)) {
            return new HashMap<>();
        }
        try {
            Map<String, OrderAccountData> accounts = JSON.fromJson(Files.readString(file), ACCOUNTS_TYPE);
            validate(accounts);
            return new HashMap<>(accounts);
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot read the practice accounts.", exception);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Practice account file is invalid; original file was retained.", exception);
        }
    }

    private void write(Map<String, OrderAccountData> accounts) {
        validate(accounts);
        Path temporary = null;
        try {
            temporary = Files.createTempFile(file.getParent(), file.getFileName() + ".", ".tmp");
            ByteBuffer data = StandardCharsets.UTF_8.encode(JSON.toJson(accounts, ACCOUNTS_TYPE));
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                while (data.hasRemaining()) {
                    channel.write(data);
                }
                channel.force(true);
            }
            // Do not fall back to a non-atomic move: failure must preserve the old account.
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            temporary = null;
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot commit the practice account.", exception);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                    // Cleanup cannot change whether the transaction committed.
                }
            }
        }
    }

    private static void validate(Map<String, OrderAccountData> accounts) {
        if (accounts == null || accounts.entrySet().stream().anyMatch(entry -> entry.getKey() == null
                || entry.getKey().isBlank() || entry.getValue() == null || entry.getValue().history().stream()
                .anyMatch(order -> !entry.getKey().equals(order.getUserID())))) {
            throw new JsonParseException("Invalid account document or trade owner.");
        }
    }

    private <T> T locked(Supplier<T> operation) {
        synchronized (processLock) {
            FileChannel channel = null;
            FileLock lock = null;
            try {
                channel = FileChannel.open(file.resolveSibling(file.getFileName() + ".lock"),
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                lock = channel.lock();
                return operation.get();
            } catch (IOException exception) {
                throw new UncheckedIOException("Cannot lock the practice accounts.", exception);
            } finally {
                try {
                    if (lock != null) {
                        lock.release();
                    }
                } catch (IOException ignored) {
                    // A release failure must not report an already persisted order as rejected.
                }
                try {
                    if (channel != null) {
                        channel.close();
                    }
                } catch (IOException ignored) {
                    // Closing the channel is cleanup, after the commit outcome was determined.
                }
            }
        }
    }
}

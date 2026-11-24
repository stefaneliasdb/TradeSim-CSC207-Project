package entity;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Portfolio {
    private final Map<String, Holding> holdings;
    private final List<Order> orderHistory;

    public Portfolio() {
        this.holdings = new HashMap<>();
        this.orderHistory = new ArrayList<>();
    }

    public List<Holding> getHoldings() {
        return new ArrayList<>(holdings.values());
    }

    public int getQuantity(String symbol) {
        Holding holding = holdings.get(symbol);
        if (holding == null) {
            return 0;
        }
        return holding.quantity();
    }

    public void addShares(String symbol, int quantity, BigDecimal pricePerShare, Instant boughtAt) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        if (pricePerShare.signum() < 0) {
            throw new IllegalArgumentException("Price per share cannot be negative.");
        }

        Holding existing = holdings.get(symbol);
        int updatedQuantity = quantity;
        BigDecimal updatedPurchaseCost = pricePerShare.multiply(BigDecimal.valueOf(quantity));
        Instant firstPurchasedAt = boughtAt;

        if (existing != null) {
            updatedQuantity = existing.quantity() + quantity;
            updatedPurchaseCost = existing.totalPurchaseCost().add(updatedPurchaseCost);
            firstPurchasedAt = existing.firstPurchasedAt();
        }
        Holding updatedHolding = new Holding(
                symbol, updatedQuantity, updatedPurchaseCost, firstPurchasedAt);
        holdings.put(symbol, updatedHolding);
    }

    public void removeShares(String symbol, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        Holding existing = holdings.get(symbol);
        if (existing == null || quantity > existing.quantity()) {
            throw new IllegalArgumentException("Cannot remove more shares than the portfolio holds.");
        }

        int remainingQuantity = existing.quantity() - quantity;
        if (remainingQuantity == 0) {
            holdings.remove(symbol);
        } else {
            BigDecimal remainingQuantityValue = BigDecimal.valueOf(remainingQuantity);
            BigDecimal existingQuantityValue = BigDecimal.valueOf(existing.quantity());
            BigDecimal remainingPurchaseCost = existing.totalPurchaseCost()
                    .multiply(remainingQuantityValue);
            remainingPurchaseCost = remainingPurchaseCost.divide(
                    existingQuantityValue, MathContext.DECIMAL128);
            Holding updatedHolding = new Holding(
                    symbol,
                    remainingQuantity,
                    remainingPurchaseCost,
                    existing.firstPurchasedAt());
            holdings.put(symbol, updatedHolding);
        }
    }

    public List<Order> getOrderHistory() {
        return new ArrayList<>(orderHistory);
    }

    public void addOrder(Order order) {
        orderHistory.add(order);
    }

    public BigDecimal getTotalValue(Map<String, BigDecimal> prices) {
        return null;
    }

}

package interface_adapter.order;

import entity.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Formats a local practice-account receipt; the team's Portfolio view remains independent. */
public final class OrderActivityPresenter {
    private final DateTimeFormatter time = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public OrderActivityState present(BigDecimal cash, Map<String, Integer> holdings, List<Order> history) {
        String holdingsText = holdings.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("   |   "));
        List<String> trades = history.stream().sorted(Comparator.comparing(Order::getExecutedAt).reversed())
                .map(order -> time.format(order.getExecutedAt()) + "   " + order.getType() + " "
                        + order.getQuantity() + " " + order.getSymbol() + "   USD "
                        + money(order.getTotalValue()))
                .toList();
        return new OrderActivityState("Cash: USD " + money(cash),
                holdingsText.isEmpty() ? "Shares: none" : "Shares: " + holdingsText, trades);
    }

    private static String money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}

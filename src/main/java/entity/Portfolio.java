package entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class Portfolio {
    private Map<String, Holding> holdings;
    private List<Order> orderHistory;

    public Portfolio() {
    }

    public List<Holding> getHoldings() {
        return null;
    }

    public int getQuantity(String symbol) {
        return 0;
    }

    public void addShares(String symbol, int quantity, BigDecimal cost, Instant boughtAt) {
    }

    public void removeShares(String symbol, int quantity) {
    }

    public List<Order> getOrderHistory() {
        return null;
    }

    public void addOrder(Order order) {
    }

    public BigDecimal getTotalValue(Map<String, BigDecimal> prices) {
        return null;
    }
}

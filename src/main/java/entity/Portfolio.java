package entity;

import java.math.BigDecimal;
import java.util.Map;

public class Portfolio {
    private Map<String, Integer> holdings;

    public Portfolio() {
    }

    public Map<String, Integer> getHoldings() {
        return null;
    }

    public int getQuantity(String symbol) {
        return 0;
    }

    public void addShares(String symbol, int quantity) {
    }

    public void removeShares(String symbol, int quantity) {
    }

    public BigDecimal getTotalValue(Map<String, BigDecimal> prices) {
        return null;
    }
}

package interface_adapter;

public class StockListState {

    private final String symbol;
    private final String companyName;
    private final String price;
    private final String dailyPercentageChange;

    public StockListState(
            String symbol,
            String companyName,
            String price,
            String dailyPercentageChange) {

        this.symbol = symbol;
        this.companyName = companyName;
        this.price = price;
        this.dailyPercentageChange = dailyPercentageChange;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getPrice() {
        return price;
    }

    public String getDailyPercentageChange() {
        return dailyPercentageChange;
    }
}
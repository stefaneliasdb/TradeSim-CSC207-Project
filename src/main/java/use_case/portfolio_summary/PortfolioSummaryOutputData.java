package use_case.portfolio_summary;

import java.util.List;

public class PortfolioSummaryOutputData {

    public static class Row {
        private final String symbol;
        private final int quantity;

        public Row(String symbol, int quantity) {
            this.symbol = symbol;
            this.quantity = quantity;
        }

        public String getSymbol() {
            return symbol;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    private final PortfolioSummaryStatus status;
    private final List<Row> rows;

    public PortfolioSummaryOutputData(PortfolioSummaryStatus status, List<Row> rows) {
        this.status = status;
        this.rows = rows;
    }

    public PortfolioSummaryStatus getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status == PortfolioSummaryStatus.LOADED;
    }

    public List<Row> getRows() {
        return rows;
    }
}

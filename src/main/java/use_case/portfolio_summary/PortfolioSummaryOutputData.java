package use_case.portfolio_summary;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class PortfolioSummaryOutputData {

    public record Row(String symbol, Instant purchasedAt,
                      BigDecimal currentValue, BigDecimal percentChange) {
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

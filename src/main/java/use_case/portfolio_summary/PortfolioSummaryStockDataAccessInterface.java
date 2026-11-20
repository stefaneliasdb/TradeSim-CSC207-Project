package use_case.portfolio_summary;

import java.math.BigDecimal;
import java.util.Optional;

public interface PortfolioSummaryStockDataAccessInterface {
    Optional<BigDecimal> findPrice(String symbol);
}

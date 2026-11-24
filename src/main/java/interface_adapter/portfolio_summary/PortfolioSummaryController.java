package interface_adapter.portfolio_summary;

import use_case.portfolio_summary.PortfolioSummaryInputBoundary;
import use_case.portfolio_summary.PortfolioSummaryInputData;

public class PortfolioSummaryController {
    private final PortfolioSummaryInputBoundary input;

    public PortfolioSummaryController(PortfolioSummaryInputBoundary input) {
        this.input = input;
    }

    public void getSummary(String userID) {
        input.execute(new PortfolioSummaryInputData(userID));
    }
}

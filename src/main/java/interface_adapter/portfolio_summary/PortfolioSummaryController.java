package interface_adapter.portfolio_summary;

import use_case.portfolio_summary.PortfolioSummaryInputBoundary;

public class PortfolioSummaryController {
    private final PortfolioSummaryInputBoundary input;

    public PortfolioSummaryController(PortfolioSummaryInputBoundary input) {
        this.input = input;
    }

    public void getSummary(String userID) {
    }
}

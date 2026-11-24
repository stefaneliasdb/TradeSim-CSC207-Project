package interface_adapter.portfolio_summary;

import use_case.portfolio_summary.PortfolioSummaryOutputBoundary;
import use_case.portfolio_summary.PortfolioSummaryOutputData;

public class PortfolioSummaryPresenter implements PortfolioSummaryOutputBoundary {
    private final PortfolioSummaryViewModel viewModel;

    public PortfolioSummaryPresenter(PortfolioSummaryViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(PortfolioSummaryOutputData result) {
    }
}

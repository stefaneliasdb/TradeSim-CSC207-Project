package interface_adapter.portfolio_summary;

import use_case.portfolio_summary.PortfolioSummaryOutputBoundary;
import use_case.portfolio_summary.PortfolioSummaryOutputData;
import use_case.portfolio_summary.PortfolioSummaryStatus;

import java.util.ArrayList;

public class PortfolioSummaryPresenter implements PortfolioSummaryOutputBoundary {
    private final PortfolioSummaryViewModel viewModel;

    public PortfolioSummaryPresenter(PortfolioSummaryViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(PortfolioSummaryOutputData result) {
        if (result.isSuccess()) {
            String message;
            if (result.getRows().isEmpty()) {
                message = "No holdings in this portfolio.";
            } else {
                message = "";
            }
            viewModel.update(message, result.getRows());
        } else if (result.getStatus() == PortfolioSummaryStatus.ACCOUNT_NOT_FOUND) {
            viewModel.update("Portfolio not found.", new ArrayList<>());
        } else {
            viewModel.update("A valid user ID is required.", new ArrayList<>());
        }
    }
}

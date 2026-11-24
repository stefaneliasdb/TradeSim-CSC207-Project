package use_case.portfolio_summary;

public class PortfolioSummaryInteractor implements PortfolioSummaryInputBoundary {
    private final PortfolioSummaryUserDataAccessInterface users;
    private final PortfolioSummaryStockDataAccessInterface stocks;
    private final PortfolioSummaryOutputBoundary output;

    public PortfolioSummaryInteractor(PortfolioSummaryUserDataAccessInterface users,
                                      PortfolioSummaryStockDataAccessInterface stocks,
                                      PortfolioSummaryOutputBoundary output) {
        this.users = users;
        this.stocks = stocks;
        this.output = output;
    }

    @Override
    public void execute(PortfolioSummaryInputData input) {
    }
}

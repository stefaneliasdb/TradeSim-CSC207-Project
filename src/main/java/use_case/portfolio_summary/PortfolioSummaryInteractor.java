package use_case.portfolio_summary;

import entity.Holding;
import entity.User;

import java.util.ArrayList;
import java.util.List;

public class PortfolioSummaryInteractor implements PortfolioSummaryInputBoundary {
    private final PortfolioSummaryUserDataAccessInterface users;
    private final PortfolioSummaryOutputBoundary output;

    public PortfolioSummaryInteractor(PortfolioSummaryUserDataAccessInterface users,
                                      PortfolioSummaryOutputBoundary output) {
        this.users = users;
        this.output = output;
    }

    @Override
    public void execute(PortfolioSummaryInputData input) {
        if (input.userID() == null || input.userID().isBlank()) {
            output.present(new PortfolioSummaryOutputData(
                    PortfolioSummaryStatus.INVALID_INPUT, new ArrayList<>()));
            return;
        }

        User user = users.findById(input.userID());
        if (user == null) {
            output.present(new PortfolioSummaryOutputData(
                    PortfolioSummaryStatus.ACCOUNT_NOT_FOUND, new ArrayList<>()));
            return;
        }

        List<PortfolioSummaryOutputData.Row> rows = new ArrayList<>();
        for (Holding holding : user.getPortfolio().getHoldings()) {
            PortfolioSummaryOutputData.Row row =
                    new PortfolioSummaryOutputData.Row(
                            holding.symbol(),
                            holding.quantity());
            rows.add(row);
        }
        output.present(new PortfolioSummaryOutputData(PortfolioSummaryStatus.LOADED, rows));
    }
}

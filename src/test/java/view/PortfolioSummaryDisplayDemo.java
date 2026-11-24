package view;

import data_access.PortfolioDataStore;
import entity.User;
import interface_adapter.portfolio_summary.PortfolioSummaryPresenter;
import interface_adapter.portfolio_summary.PortfolioSummaryViewModel;
import use_case.portfolio_summary.PortfolioSummaryInteractor;
import use_case.portfolio_summary.PortfolioSummaryInputData;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.math.BigDecimal;
import java.time.Instant;

public class PortfolioSummaryDisplayDemo {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> showPortfolioSummary());
    }

    private static void showPortfolioSummary() {
        PortfolioDataStore users = new PortfolioDataStore();
        User user = new User("demo", "password");
        user.getPortfolio().addShares("AAPL", 5, new BigDecimal("180.00"), Instant.now());
        user.getPortfolio().addShares("MSFT", 3, new BigDecimal("400.00"), Instant.now());
        users.saveUser(user);

        PortfolioSummaryViewModel viewModel = new PortfolioSummaryViewModel();
        PortfolioSummaryPresenter presenter = new PortfolioSummaryPresenter(viewModel);
        PortfolioSummaryInteractor interactor =
                new PortfolioSummaryInteractor(users, presenter);
        interactor.execute(new PortfolioSummaryInputData(user.getuserId()));

        JFrame frame = new JFrame("Portfolio Summary Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new PortfolioSummaryView(viewModel));
        frame.setPreferredSize(new Dimension(400, 250));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

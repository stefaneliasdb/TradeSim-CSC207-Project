package app;

import data_access.StockMarketDataAccessObject;
import interface_adapter.ViewStockListController;
import interface_adapter.ViewStockListPresenter;
import interface_adapter.ViewStockListViewModel;
import use_case.ViewStockListInteractor;
import view.ViewStockListView;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            ViewStockListViewModel viewModel =
                    new ViewStockListViewModel();

            ViewStockListPresenter presenter =
                    new ViewStockListPresenter(viewModel);

            StockMarketDataAccessObject dataAccess =
                    new StockMarketDataAccessObject();

            ViewStockListInteractor interactor =
                    new ViewStockListInteractor(
                            dataAccess, presenter);

            ViewStockListController controller =
                    new ViewStockListController(interactor);

            ViewStockListView stockView =
                    new ViewStockListView(controller, viewModel);

            JFrame frame = new JFrame("TradeSim");
            frame.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE);

            frame.setLayout(new BorderLayout());
            frame.add(stockView, BorderLayout.WEST);

            JPanel mainContent = new JPanel(
                    new BorderLayout());

            JLabel placeholder = new JLabel(
                    "Select a stock to view details",
                    SwingConstants.CENTER);

            mainContent.add(
                    placeholder, BorderLayout.CENTER);

            frame.add(mainContent, BorderLayout.CENTER);

            frame.setSize(1000, 650);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
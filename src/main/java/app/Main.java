package app;

import data_access.StockMarketDataAccessObject;
import interface_adapter.ViewStockChartController;
import interface_adapter.ViewStockChartPresenter;
import interface_adapter.ViewStockChartViewModel;
import interface_adapter.ViewStockListController;
import interface_adapter.ViewStockListPresenter;
import interface_adapter.ViewStockListViewModel;
import use_case.ViewStockChartInteractor;
import use_case.ViewStockChartOutputBoundary;
import use_case.ViewStockListInteractor;
import view.ViewStockChartView;
import view.ViewStockListView;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // Shared data access object.
            StockMarketDataAccessObject dataAccess =
                    new StockMarketDataAccessObject();

            // Stock list setup.
            ViewStockListViewModel stockListViewModel =
                    new ViewStockListViewModel();

            ViewStockListPresenter stockListPresenter =
                    new ViewStockListPresenter(stockListViewModel);

            ViewStockListInteractor stockListInteractor =
                    new ViewStockListInteractor(
                            dataAccess, stockListPresenter);

            ViewStockListController stockListController =
                    new ViewStockListController(stockListInteractor);

            ViewStockListView stockListView =
                    new ViewStockListView(
                            stockListController, stockListViewModel);

            // Stock chart setup.
            ViewStockChartViewModel chartViewModel =
                    new ViewStockChartViewModel();

            ViewStockChartPresenter chartPresenter =
                    new ViewStockChartPresenter(chartViewModel);

            ViewStockChartView chartView =
                    new ViewStockChartView(chartViewModel);

            // Each selection receives a unique request number.
            AtomicLong latestRequest = new AtomicLong();

            // Listen for stock selection changes.
            stockListViewModel.addPropertyChangeListener(event -> {

                if (!"selectedSymbol".equals(
                        event.getPropertyName())) {
                    return;
                }

                String symbol = (String) event.getNewValue();

                // Invalidate any previous request.
                long requestId = latestRequest.incrementAndGet();

                if (symbol == null) {
                    return;
                }

                // Give this request its own output boundary.
                // Only the latest request may update the chart.
                ViewStockChartOutputBoundary guardedPresenter =
                        new ViewStockChartOutputBoundary() {

                            @Override
                            public void prepareSuccessView(
                                    String resultSymbol,
                                    List<entity.HistoricalPrice> prices) {

                                SwingUtilities.invokeLater(() -> {
                                    if (requestId == latestRequest.get()) {
                                        chartPresenter.prepareSuccessView(
                                                resultSymbol, prices);
                                    }
                                });
                            }

                            @Override
                            public void prepareFailView(
                                    String errorMessage) {

                                SwingUtilities.invokeLater(() -> {
                                    if (requestId == latestRequest.get()) {
                                        chartPresenter.prepareFailView(
                                                errorMessage);
                                    }
                                });
                            }
                        };

                // Run network requests off the Swing UI thread.
                new SwingWorker<Void, Void>() {

                    @Override
                    protected Void doInBackground() {

                        ViewStockChartInteractor chartInteractor =
                                new ViewStockChartInteractor(
                                        dataAccess, guardedPresenter);

                        ViewStockChartController chartController =
                                new ViewStockChartController(
                                        chartInteractor);

                        chartController.execute(symbol);

                        return null;
                    }

                }.execute();
            });

            // Main application window.
            JFrame frame = new JFrame("TradeSim");

            frame.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE);

            frame.setLayout(new BorderLayout());

            frame.add(stockListView, BorderLayout.WEST);
            frame.add(chartView, BorderLayout.CENTER);

            frame.setSize(1100, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
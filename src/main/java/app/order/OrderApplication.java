package app.order;

import data_access.StockMarketDataAccessObject;
import data_access.order.FileOrderAccountStore;
import data_access.order.OrderAccountAdapter;
import data_access.order.OrderAccountData;
import data_access.order.StockOrderQuoteGateway;
import entity.HistoricalPrice;
import interface_adapter.ViewStockChartController;
import interface_adapter.ViewStockChartPresenter;
import interface_adapter.ViewStockChartViewModel;
import interface_adapter.ViewStockListController;
import interface_adapter.ViewStockListPresenter;
import interface_adapter.ViewStockListViewModel;
import interface_adapter.order.buy.BuyOrderController;
import interface_adapter.order.buy.BuyOrderPresenter;
import interface_adapter.order.OrderActivityPresenter;
import interface_adapter.order.OrderViewModel;
import interface_adapter.order.sell.SellOrderController;
import interface_adapter.order.sell.SellOrderPresenter;
import use_case.ViewStockChartDataAccessInterface;
import use_case.ViewStockChartInteractor;
import use_case.ViewStockChartOutputBoundary;
import use_case.ViewStockListDataAccessInterface;
import use_case.ViewStockListInteractor;
import use_case.order.buy.BuyOrderInteractor;
import use_case.order.sell.SellOrderInteractor;
import view.OrderActivityView;
import view.OrderView;
import view.ViewStockChartView;
import view.ViewStockListView;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Order-only composition root. Run with --demo for fixed offline prices; without it, use
 * TWELVE_DATA_API_KEY as required by the team's existing market-data DAO.
 * The practice account is deliberately separate from the team's unfinished login/Portfolio integration.
 */
public final class OrderApplication {
    private static final String PRACTICE_USER = "local-trader";

    private OrderApplication() { }

    public static void main(String[] args) {
        boolean demo = Arrays.asList(args).contains("--demo");
        try {
            Path dataFile = Path.of(System.getProperty("tradesim.order.data", Path.of(
                    System.getProperty("user.home"), ".tradesim", demo ? "order-demo.json" : "order-practice.json")
                    .toString()));
            FileOrderAccountStore store = new FileOrderAccountStore(dataFile);
            store.create(PRACTICE_USER, new OrderAccountData(new BigDecimal("10000.00"), Map.of(), List.of()));
            ViewStockListDataAccessInterface stocks;
            ViewStockChartDataAccessInterface charts;
            if (demo) {
                DemoOrderMarketData market = new DemoOrderMarketData();
                stocks = market;
                charts = market;
            } else {
                StockMarketDataAccessObject market = new StockMarketDataAccessObject();
                stocks = market;
                charts = market;
            }
            SwingUtilities.invokeLater(() -> showWindow(store, stocks, charts, demo));
        } catch (RuntimeException exception) {
            // Do not echo provider details, which could contain an API key or other credentials.
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "Cannot open Order. Check the account file and, for live prices, TWELVE_DATA_API_KEY.\n"
                            + "Use the Order Demo run configuration for offline prices.",
                    "Order startup failed", JOptionPane.ERROR_MESSAGE));
        }
    }

    private static void showWindow(FileOrderAccountStore store, ViewStockListDataAccessInterface stocks,
                                   ViewStockChartDataAccessInterface charts, boolean demo) {
        OrderAccountAdapter accounts = new OrderAccountAdapter(store);
        StockOrderQuoteGateway quotes = new StockOrderQuoteGateway(stocks, "USD");
        OrderViewModel orderModel = new OrderViewModel();
        BuyOrderController buy = new BuyOrderController(new BuyOrderInteractor(quotes, accounts,
                new BuyOrderPresenter(orderModel), Clock.systemUTC()));
        SellOrderController sell = new SellOrderController(new SellOrderInteractor(quotes, accounts,
                new SellOrderPresenter(orderModel), Clock.systemUTC()));

        OrderActivityView activity = new OrderActivityView();
        OrderActivityPresenter activityPresenter = new OrderActivityPresenter();
        AtomicLong latestRefresh = new AtomicLong();
        Consumer<String> refresh = userID -> {
            long request = latestRefresh.incrementAndGet();
            new SwingWorker<OrderAccountData, Void>() {
                @Override
                protected OrderAccountData doInBackground() {
                    return store.find(userID).orElseThrow();
                }

                @Override
                protected void done() {
                    if (request != latestRefresh.get()) {
                        return;
                    }
                    try {
                        OrderAccountData account = get();
                        activity.display(activityPresenter.present(account.cash(), account.holdings(), account.history()));
                    } catch (Exception exception) {
                        activity.showRefreshError();
                    }
                }
            }.execute();
        };
        OrderView order = new OrderView(buy, sell, orderModel, () -> PRACTICE_USER, refresh);
        order.setSymbol("AAPL");

        ViewStockChartViewModel chartModel = new ViewStockChartViewModel();
        ViewStockChartPresenter chartPresenter = new ViewStockChartPresenter(chartModel);
        ViewStockChartView chart = new ViewStockChartView(chartModel);
        ViewStockListViewModel stockModel = new ViewStockListViewModel();
        AtomicLong latestChart = new AtomicLong();
        stockModel.addPropertyChangeListener(event -> {
            if ("selectedSymbol".equals(event.getPropertyName()) && event.getNewValue() instanceof String symbol) {
                order.setSymbol(symbol);
                loadChart(charts, chartPresenter, symbol, latestChart);
            }
        });
        // Marshal the existing list presenter's notifications onto the UI thread.
        ViewStockListPresenter listPresenter = new ViewStockListPresenter(stockModel);
        use_case.ViewStockListOutputBoundary listOutput = new use_case.ViewStockListOutputBoundary() {
            @Override
            public void prepareSuccessView(use_case.ViewStockListOutputData output) {
                SwingUtilities.invokeLater(() -> listPresenter.prepareSuccessView(output));
            }

            @Override
            public void prepareFailView(String message) {
                SwingUtilities.invokeLater(() -> listPresenter.prepareFailView(message));
            }
        };
        ViewStockListView stockList = new ViewStockListView(new ViewStockListController(
                new ViewStockListInteractor(stocks, listOutput)), stockModel);

        JPanel orderPanel = new JPanel(new BorderLayout(8, 12));
        orderPanel.setPreferredSize(new Dimension(450, 650));
        orderPanel.add(order, BorderLayout.NORTH);
        orderPanel.add(activity, BorderLayout.CENTER);
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        content.add(new JLabel(demo
                ? "ORDER DEMO · Fixed sample prices · Local practice account (no real trading)"
                : "ORDER PRACTICE · Twelve Data quotes · Local practice account (no real trading)"), BorderLayout.NORTH);
        content.add(stockList, BorderLayout.WEST);
        content.add(chart, BorderLayout.CENTER);
        content.add(orderPanel, BorderLayout.EAST);
        JFrame frame = new JFrame(demo ? "TradeSim — Order Demo" : "TradeSim — Order Practice");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(content);
        frame.setMinimumSize(new Dimension(1150, 650));
        frame.setSize(1420, 820);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        refresh.accept(PRACTICE_USER);
        stockModel.setSelectedSymbol("AAPL");
    }

    private static void loadChart(ViewStockChartDataAccessInterface source, ViewStockChartPresenter presenter,
                                  String symbol, AtomicLong latestRequest) {
        long request = latestRequest.incrementAndGet();
        ViewStockChartOutputBoundary output = new ViewStockChartOutputBoundary() {
            @Override
            public void prepareSuccessView(String resultSymbol, List<HistoricalPrice> prices) {
                SwingUtilities.invokeLater(() -> {
                    if (request == latestRequest.get()) {
                        presenter.prepareSuccessView(resultSymbol, prices);
                    }
                });
            }

            @Override
            public void prepareFailView(String message) {
                SwingUtilities.invokeLater(() -> {
                    if (request == latestRequest.get()) {
                        presenter.prepareFailView("Chart unavailable for " + symbol);
                    }
                });
            }
        };
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                new ViewStockChartController(new ViewStockChartInteractor(source, output)).execute(symbol);
                return null;
            }
        }.execute();
    }
}

package view;

import interface_adapter.StockListState;
import interface_adapter.ViewStockListController;
import interface_adapter.ViewStockListViewModel;

import javax.swing.*;
import java.awt.*;

public class ViewStockListView extends JPanel {

    private final ViewStockListViewModel viewModel;
    private final JPanel stockPanel;

    public ViewStockListView(
            ViewStockListController controller,
            ViewStockListViewModel viewModel) {

        this.viewModel = viewModel;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(280, 600));

        JLabel title = new JLabel("Stocks");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(15, 12, 15, 12));
        add(title, BorderLayout.NORTH);

        stockPanel = new JPanel();
        stockPanel.setLayout(
                new BoxLayout(stockPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(stockPanel);
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        viewModel.addPropertyChangeListener(event -> {
            if ("stocks".equals(event.getPropertyName())) {
                SwingUtilities.invokeLater(this::refreshStocks);
            }
        });

        controller.execute();
    }

    private void refreshStocks() {
        stockPanel.removeAll();

        for (StockListState stock : viewModel.getStocks()) {
            JPanel row = new JPanel(new BorderLayout(8, 4));
            row.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            row.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 65));

            JPanel namePanel = new JPanel();
            namePanel.setLayout(
                    new BoxLayout(namePanel, BoxLayout.Y_AXIS));

            JLabel symbol = new JLabel(stock.getSymbol());
            symbol.setFont(new Font("SansSerif", Font.BOLD, 14));

            JLabel company = new JLabel(stock.getCompanyName());
            company.setFont(new Font("SansSerif", Font.PLAIN, 11));

            namePanel.add(symbol);
            namePanel.add(company);

            JPanel pricePanel = new JPanel();
            pricePanel.setLayout(
                    new BoxLayout(pricePanel, BoxLayout.Y_AXIS));

            JLabel price = new JLabel(stock.getPrice());
            price.setFont(new Font("SansSerif", Font.BOLD, 12));

            JLabel change = new JLabel(
                    stock.getDailyPercentageChange());

            String changeText = stock.getDailyPercentageChange();

            if (changeText.startsWith("+")) {
                change.setForeground(new Color(0, 140, 70));
            } else if (changeText.startsWith("-")) {
                change.setForeground(new Color(190, 40, 40));
            }

            pricePanel.add(price);
            pricePanel.add(change);

            row.add(namePanel, BorderLayout.WEST);
            row.add(pricePanel, BorderLayout.EAST);

            stockPanel.add(row);
            stockPanel.add(new JSeparator());
        }

        stockPanel.revalidate();
        stockPanel.repaint();
    }
}
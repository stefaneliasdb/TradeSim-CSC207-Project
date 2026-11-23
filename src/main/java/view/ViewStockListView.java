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

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBorder(
                    BorderFactory.createEmptyBorder(12, 10, 12, 10));
            row.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 75));

            // LEFT: Stock symbol and company name
            JPanel namePanel = new JPanel();
            namePanel.setLayout(
                    new BoxLayout(namePanel, BoxLayout.Y_AXIS));

            JLabel symbol = new JLabel(stock.getSymbol());
            symbol.setFont(
                    new Font("SansSerif", Font.BOLD, 14));

            String companyName = stock.getCompanyName();

            // Shorten long company names
            if (companyName.length() > 18) {
                companyName = companyName.substring(0, 15) + "...";
            }

            JLabel company = new JLabel(companyName);
            company.setFont(
                    new Font("SansSerif", Font.PLAIN, 11));

            namePanel.add(symbol);
            namePanel.add(company);

            // RIGHT: Price and daily percentage change
            JPanel pricePanel = new JPanel();
            pricePanel.setLayout(
                    new BoxLayout(pricePanel, BoxLayout.Y_AXIS));

            pricePanel.setPreferredSize(new Dimension(100, 45));

            JLabel price = new JLabel(stock.getPrice());
            price.setFont(
                    new Font("SansSerif", Font.BOLD, 12));

            JLabel change = new JLabel(
                    stock.getDailyPercentageChange());

            change.setFont(
                    new Font("SansSerif", Font.PLAIN, 11));

            String changeText = stock.getDailyPercentageChange();

            if (changeText.startsWith("+")) {
                change.setForeground(new Color(0, 140, 70));
            } else if (changeText.startsWith("-")) {
                change.setForeground(new Color(190, 40, 40));
            }

            price.setAlignmentX(Component.RIGHT_ALIGNMENT);
            change.setAlignmentX(Component.RIGHT_ALIGNMENT);

            pricePanel.add(price);
            pricePanel.add(change);

            row.add(namePanel, BorderLayout.CENTER);
            row.add(pricePanel, BorderLayout.EAST);

            stockPanel.add(row);
            stockPanel.add(new JSeparator());
        }

        stockPanel.revalidate();
        stockPanel.repaint();
    }
}
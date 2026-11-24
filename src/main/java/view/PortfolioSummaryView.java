package view;

import interface_adapter.portfolio_summary.PortfolioSummaryViewModel;
import use_case.portfolio_summary.PortfolioSummaryOutputData;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

public class PortfolioSummaryView extends JPanel {
    private final PortfolioSummaryViewModel viewModel;
    private final JPanel holdingsPanel = new JPanel();
    private final JLabel messageLabel = new JLabel();

    public PortfolioSummaryView(PortfolioSummaryViewModel viewModel) {
        this.viewModel = viewModel;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(280, 600));

        JLabel title = new JLabel("Portfolio");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(15, 12, 15, 12));
        add(title, BorderLayout.NORTH);

        holdingsPanel.setLayout(new BoxLayout(holdingsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(holdingsPanel);
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        messageLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        add(messageLabel, BorderLayout.SOUTH);

        viewModel.addPropertyChangeListener(event ->
                SwingUtilities.invokeLater(() -> showSummary()));
        showSummary();
    }

    private void showSummary() {
        holdingsPanel.removeAll();
        for (PortfolioSummaryOutputData.Row holding : viewModel.getRows()) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

            JLabel symbol = new JLabel(holding.getSymbol());
            symbol.setFont(new Font("SansSerif", Font.BOLD, 14));
            JLabel quantity = new JLabel("Quantity: " + holding.getQuantity());
            quantity.setFont(new Font("SansSerif", Font.PLAIN, 12));

            row.add(symbol, BorderLayout.CENTER);
            row.add(quantity, BorderLayout.EAST);
            holdingsPanel.add(row);
            holdingsPanel.add(new JSeparator());
        }

        messageLabel.setText(viewModel.getMessage());
        holdingsPanel.revalidate();
        holdingsPanel.repaint();
    }
}

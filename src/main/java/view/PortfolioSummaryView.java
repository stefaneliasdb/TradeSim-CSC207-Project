package view;

import interface_adapter.portfolio_summary.PortfolioSummaryController;
import interface_adapter.portfolio_summary.PortfolioSummaryViewModel;

import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PortfolioSummaryView extends JPanel implements ActionListener, PropertyChangeListener {
    private final PortfolioSummaryController controller;
    private final PortfolioSummaryViewModel viewModel;

    public PortfolioSummaryView(PortfolioSummaryController controller, PortfolioSummaryViewModel viewModel) {
        this.controller = controller;
        this.viewModel = viewModel;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }
}

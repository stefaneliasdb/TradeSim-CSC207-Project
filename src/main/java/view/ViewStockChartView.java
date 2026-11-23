package view;

import interface_adapter.StockChartState;
import interface_adapter.ViewStockChartViewModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Date;

public class ViewStockChartView extends JPanel {

    private final ViewStockChartViewModel viewModel;
    private final JLabel title;
    private final JPanel chartContainer;

    public ViewStockChartView(ViewStockChartViewModel viewModel) {
        this.viewModel = viewModel;

        setLayout(new BorderLayout());

        title = new JLabel("Select a stock to view its chart");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(title, BorderLayout.NORTH);

        chartContainer = new JPanel(new BorderLayout());
        add(chartContainer, BorderLayout.CENTER);

        viewModel.addPropertyChangeListener(event -> {
            if ("chartData".equals(event.getPropertyName())) {
                SwingUtilities.invokeLater(this::refreshChart);
            } else if ("errorMessage".equals(event.getPropertyName())) {
                SwingUtilities.invokeLater(this::showError);
            }
        });
    }

    private void refreshChart() {
        String symbol = viewModel.getSymbol();

        title.setText(symbol + " — Historical Price (1 Month)");

        TimeSeries series = new TimeSeries(symbol);

        for (StockChartState price : viewModel.getPrices()) {
            LocalDate date = price.getDate();

            series.addOrUpdate(
                    new Day(
                            date.getDayOfMonth(),
                            date.getMonthValue(),
                            date.getYear()
                    ),
                    price.getClosingPrice().doubleValue()
            );
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                null,
                "Date",
                "Closing Price",
                dataset,
                false,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();

        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        if (plot.getDomainAxis() instanceof DateAxis dateAxis) {
            dateAxis.setDateFormatOverride(
                    new java.text.SimpleDateFormat("MMM d"));
        }

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);

        chartContainer.removeAll();
        chartContainer.add(chartPanel, BorderLayout.CENTER);

        chartContainer.revalidate();
        chartContainer.repaint();
    }

    private void showError() {
        chartContainer.removeAll();

        JLabel errorLabel = new JLabel(
                viewModel.getErrorMessage(),
                SwingConstants.CENTER
        );

        errorLabel.setForeground(Color.RED);
        chartContainer.add(errorLabel, BorderLayout.CENTER);

        chartContainer.revalidate();
        chartContainer.repaint();
    }
}
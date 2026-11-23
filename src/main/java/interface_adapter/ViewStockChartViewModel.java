package interface_adapter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

public class ViewStockChartViewModel {

    private final PropertyChangeSupport support =
            new PropertyChangeSupport(this);

    private String symbol = "";
    private List<StockChartState> prices = List.of();
    private String errorMessage = "";

    public String getSymbol() {
        return symbol;
    }

    public List<StockChartState> getPrices() {
        return prices;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setChartData(
            String symbol,
            List<StockChartState> prices) {

        this.symbol = symbol;
        this.prices = List.copyOf(prices);
        this.errorMessage = "";

        support.firePropertyChange(
                "chartData", null, this.prices);
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.prices = List.of();

        support.firePropertyChange(
                "errorMessage", null, errorMessage);
    }

    public void addPropertyChangeListener(
            PropertyChangeListener listener) {

        support.addPropertyChangeListener(listener);
    }
}
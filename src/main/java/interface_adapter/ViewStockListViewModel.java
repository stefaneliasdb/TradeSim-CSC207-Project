package interface_adapter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Objects;

public class ViewStockListViewModel {

    private final PropertyChangeSupport support =
            new PropertyChangeSupport(this);

    private List<StockListState> stocks = List.of();
    private String errorMessage = "";
    private String selectedSymbol = null;

    public List<StockListState> getStocks() {
        return stocks;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getSelectedSymbol() {
        return selectedSymbol;
    }

    public void setStocks(List<StockListState> stocks) {
        this.stocks = List.copyOf(stocks);
        support.firePropertyChange("stocks", null, this.stocks);
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        support.firePropertyChange(
                "errorMessage", null, errorMessage);
    }

    public void setSelectedSymbol(String selectedSymbol) {
        String oldSymbol = this.selectedSymbol;

        if (Objects.equals(oldSymbol, selectedSymbol)) {
            return;
        }

        this.selectedSymbol = selectedSymbol;

        support.firePropertyChange(
                "selectedSymbol",
                oldSymbol,
                selectedSymbol
        );
    }

    public void addPropertyChangeListener(
            PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}
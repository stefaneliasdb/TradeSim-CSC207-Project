package interface_adapter.portfolio_summary;

import use_case.portfolio_summary.PortfolioSummaryOutputData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class PortfolioSummaryViewModel {
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private String message = "";
    private List<PortfolioSummaryOutputData.Row> rows = new ArrayList<>();

    public PortfolioSummaryViewModel() {
    }

    public String getMessage() {
        return message;
    }

    public List<PortfolioSummaryOutputData.Row> getRows() {
        return rows;
    }

    public void update(String message, List<PortfolioSummaryOutputData.Row> rows) {
        String oldMessage = this.message;
        List<PortfolioSummaryOutputData.Row> oldRows = this.rows;
        this.message = message;
        this.rows = rows;
        support.firePropertyChange("message", oldMessage, message);
        support.firePropertyChange("rows", oldRows, this.rows);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}

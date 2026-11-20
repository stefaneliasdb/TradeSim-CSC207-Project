package interface_adapter.portfolio_summary;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

public class PortfolioSummaryViewModel {
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private String message;
    private List<String[]> rows;

    public PortfolioSummaryViewModel() {
    }

    public String getMessage() {
        return message;
    }

    public List<String[]> getRows() {
        return rows;
    }

    public void update(String message, List<String[]> rows) {
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }
}

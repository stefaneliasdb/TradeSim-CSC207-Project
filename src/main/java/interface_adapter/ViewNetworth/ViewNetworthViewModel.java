package interface_adapter.ViewNetworth;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class ViewNetworthViewModel {
    private Map<LocalTime, BigDecimal> networthHistory;
    private List<ViewNetworthViewModelListener> Listeners;

    public void setNetworthHistory(Map<LocalTime, BigDecimal> networthHistory) {
        this.networthHistory = networthHistory;
    }

    public void firePropertyChanged() {
        for (ViewNetworthViewModelListener listener : Listeners) {
            listener.notifyPropertyChanged(networthHistory);
        }
    }

    public void addListener(ViewNetworthViewModelListener listener) {
        Listeners.add(listener);
    }
}

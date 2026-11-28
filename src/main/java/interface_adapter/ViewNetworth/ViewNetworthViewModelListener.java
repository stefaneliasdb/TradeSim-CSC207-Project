package interface_adapter.ViewNetworth;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Map;

public interface ViewNetworthViewModelListener {

    void notifyPropertyChanged(Map<LocalTime, BigDecimal> networthHistory);
}

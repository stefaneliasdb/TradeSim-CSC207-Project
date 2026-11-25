package use_case.view_networth;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Map;

public class ViewNetworthOutputData {
    Map<LocalTime, BigDecimal> networthHistory;

    public ViewNetworthOutputData(Map<LocalTime, BigDecimal> networthHistory) {
        this.networthHistory = networthHistory;
    }

    public Map<LocalTime, BigDecimal> getNetworthHistory() {
        return networthHistory;
    }
}

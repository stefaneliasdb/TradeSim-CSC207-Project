package use_case.view_networth;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Map;

public interface ViewNetworthDataAccessInterface {

    Map<LocalTime, BigDecimal> getHistory(String userId);
}

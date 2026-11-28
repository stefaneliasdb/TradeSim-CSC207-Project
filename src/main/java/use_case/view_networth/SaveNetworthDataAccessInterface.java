package use_case.view_networth;

import entity.User;

import java.math.BigDecimal;
import java.time.LocalTime;

public interface SaveNetworthDataAccessInterface {
    void saveNetworth(String userId, String userName, String password,
                      LocalTime timePeriod, BigDecimal networth);
    User getUser(String userId);
}

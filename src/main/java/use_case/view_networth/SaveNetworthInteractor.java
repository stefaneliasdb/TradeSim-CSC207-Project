package use_case.view_networth;

import entity.User;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SaveNetworthInteractor {
    private SaveNetworthDataAccessInterface saveNetworthDataAccessObject;
    // timed save of networth, start timer after logged in?

    private BigDecimal getPortfolioWorth(User user) {
        // Since getHoldings, getTotalValue, is not usable right now, i will create
        // a mock dataset for demo
        Map<String, BigDecimal> stockToPrice = Map.of(
                "A", new BigDecimal("10.0"),
                "B", new BigDecimal("100.0"),
                "C", new BigDecimal("35"),
                "D", new BigDecimal("0.3"),
                "E", new BigDecimal("0.2")
        );
        return user.getPortfolio().getTotalValue(stockToPrice);
    }

    private void repeatTask(int period, User currentUser) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable saveTask = () -> {
            saveNetworthDataAccessObject.saveNetworth(
                    currentUser.getuserId(),
                    LocalTime.now(),
                    getPortfolioWorth(currentUser).add(currentUser.getUserCash())
            );
        };

        scheduler.scheduleAtFixedRate(saveTask, 0, period, TimeUnit.MINUTES);
    }

    public void execute(SaveNetworthInputData saveNetworthInputData) {
        User currentUser = saveNetworthDataAccessObject.getUser(
                saveNetworthInputData.getUserId()
        );
        // can change later to give user a choice of viewing different time interval data
        // if on an actual server we can probably keep this always recording...
        repeatTask(1, currentUser);
    }
}

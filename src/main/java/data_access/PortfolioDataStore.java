package data_access;

import entity.User;
import use_case.portfolio_summary.PortfolioSummaryUserDataAccessInterface;

import java.util.HashMap;
import java.util.Map;

public class PortfolioDataStore implements PortfolioSummaryUserDataAccessInterface {
    private final Map<String, User> users = new HashMap<>();

    public PortfolioDataStore() {
    }

    @Override
    public User findById(String userId) {
        return users.get(userId);
    }

    public void saveUser(User user) {
        users.put(user.getuserId(), user);
    }
}

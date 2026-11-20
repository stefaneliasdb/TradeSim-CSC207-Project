package use_case.portfolio_summary;

import entity.User;

import java.util.Optional;

public interface PortfolioSummaryUserDataAccessInterface {
    Optional<User> findById(String userID);
}

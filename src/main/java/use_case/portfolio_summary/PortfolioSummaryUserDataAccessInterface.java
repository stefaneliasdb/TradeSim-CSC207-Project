package use_case.portfolio_summary;

import entity.User;

public interface PortfolioSummaryUserDataAccessInterface {
    User findById(String userID);
}

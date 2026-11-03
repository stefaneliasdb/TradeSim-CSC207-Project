package entity;
import java.math.BigDecimal;
import java.util.List;

public class User {
    private static final BigDecimal DEFAULT = new BigDecimal(10000);
    private final String userId;
    private BigDecimal userCash = DEFAULT;
    // need to implement constant such as DEFAULT
    private final Portfolio portfolio;
    private String userName;
    private String password;
    private static int suffixId;


    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.portfolio = new Portfolio();
        // will update this once Portfolio class is implemented
        this.userId = userName + ( (Integer) suffixId).toString();
        suffixId += 1;
        // For now this will assign "unique" userId, more on this later
        // log in feature will be added later but I gotta learn how to do it first
    }

    private String getuserId() {
        return userId;
    }

    private String getUserName() {
        return userName;
    }

    private String getPassword() {
        return password;
    }

    private Portfolio getPortfolio() {
        return portfolio;
    }

    private BigDecimal getUserCash() {
        return userCash;
    }

    private boolean hasSufficientCash(BigDecimal amount) {
        return (this.userCash.compareTo(amount) >= 0);
    }

    /**
     *
     * @param amount the amount of cash to be added or subtracted
     * @throws IllegalArgumentException if amount is null
     */
    public void updateCash(BigDecimal amount) {
        if (amount != null) {
            this.userCash = this.userCash.add(amount);
        } else {
            throw new IllegalArgumentException("Amount is null");
        }

    }

    /**
     *
     * @param stock the stock to be added or removed for the user's portfolio
     * @param amount the amount of stock
     */
    public void updatePortfolio(Stock stock, int amount) {
        if (stock != null && amount != 0) {
            // will need to way to add/remove stocks to Portfolio
        }
    }

}

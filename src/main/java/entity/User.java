package entity;
import java.math.BigDecimal;

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

    public String getuserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public BigDecimal getUserCash() {
        return userCash;
    }

    public boolean hasSufficientCash(BigDecimal amount) {
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

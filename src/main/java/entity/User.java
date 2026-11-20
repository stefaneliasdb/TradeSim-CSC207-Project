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
}

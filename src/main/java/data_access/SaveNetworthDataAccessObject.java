package data_access;

import entity.User;
import use_case.view_networth.SaveNetworthDataAccessInterface;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalTime;

public class SaveNetworthDataAccessObject implements SaveNetworthDataAccessInterface {
    private final String url = NetworthDataConfig.getUrl();

    public SaveNetworthDataAccessObject() {
        String sql = "CREATE TABLE IF NOT EXISTS userInfo (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "userId TEXT PRIMARY KEY," +
                "userName TEXT NOT NULL," +
                "password TEXT NOT NULL," +
                "timePeriod TEXT NOT NULL," +
                "userNetworth TEXT NOT NULL )";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void saveNetworth(String userId, String userName, String password,
                             LocalTime timePeriod, BigDecimal networth) {
        String sql = "INSERT INTO userInfo (userId, ,userName, password, timePeriod, networth) VALUES (?, ?, ?, ? ,?) ";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql);) {
            pstmt.setString(1, userId);
            pstmt.setString(2, userName);
            pstmt.setString(3, password);
            pstmt.setString(4, timePeriod.toString());
            pstmt.setString(5, networth.toString());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User getUser(String userId) {
        String sql = "SELECT userName, password FROM userInfo WHERE userId = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql);) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String userName = rs.getString("userName");
                    String password = rs.getString("password");
                    User user = new User(userName, password);
                    // TODO make a runtime DAO for portfolio specifically and retreive the portfolio
                    // TODO and use setPortfolio()
                    return user;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}

// Notes: each time you log out, you lose your portfolio (MVP only saves your networth while App is open)

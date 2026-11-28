package data_access;

import entity.User;
import use_case.view_networth.ViewNetworthDataAccessInterface;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class ViewNetworthDataAccessObject implements ViewNetworthDataAccessInterface {

    private final String url = NetworthDataConfig.getUrl();

    @Override
    public Map<LocalTime, BigDecimal> getHistory(String userId) {
        Map<LocalTime, BigDecimal> networthHistory = new LinkedHashMap<>();
        String sql = "SELECT timePeriod, userNetworth FROM userInfo WHERE userId = ? ORDER BY id ASC";

        try (Connection conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sql);) {

            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LocalTime timePeriod = LocalTime.parse(rs.getString("timePeriod"));
                    BigDecimal networth = new BigDecimal(rs.getString("userNetworth"));
                    networthHistory.put(timePeriod, networth);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return networthHistory;
    }
}

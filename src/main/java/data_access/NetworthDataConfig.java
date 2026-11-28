package data_access;

public class NetworthDataConfig {
    static final String url = "jdbc:sqlite:userInfo.db";

    public static String getUrl() {
        return url;
    }
}

package use_case.view_networth;

public class SaveNetworthInputData {
    private String userId;

    public SaveNetworthInputData(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}

package interface_adapter.ViewNetworth;

import use_case.view_networth.SaveNetworthInputBoundary;
import use_case.view_networth.SaveNetworthInputData;

public class SaveNetworthController {
    private SaveNetworthInputBoundary saveNetworthInteractor;

    public SaveNetworthController(SaveNetworthInputBoundary saveNetworthInputBoundary) {
        this.saveNetworthInteractor = saveNetworthInputBoundary;
    }

    // execute will be triggered by logging in (not done)
    public void execute(String userId) {
        // userId is probably gonna be an attribute in viewmodel or viewstate somewhere
        saveNetworthInteractor.execute(
                new SaveNetworthInputData(userId)
        );
    }
}

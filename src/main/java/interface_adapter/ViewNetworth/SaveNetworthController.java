package interface_adapter.ViewNetworth;

import use_case.view_networth.SaveNetworthInputBoundary;
import use_case.view_networth.SaveNetworthInputData;

public class SaveNetworthController {
    private SaveNetworthInputBoundary saveNetworthInteractor;

    public SaveNetworthController(SaveNetworthInputBoundary saveNetworthInputBoundary) {
        this.saveNetworthInteractor = saveNetworthInputBoundary;
    }

    // TODO execute will be triggered by logging in (not done)
    public void execute(String userId) {
        // TODO userId is gonna be an attribute in viewmodel after log-in is done
        saveNetworthInteractor.execute(
                new SaveNetworthInputData(userId)
        );
    }
}

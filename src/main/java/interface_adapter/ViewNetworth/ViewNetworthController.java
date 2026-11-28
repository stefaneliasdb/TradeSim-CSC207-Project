package interface_adapter.ViewNetworth;

import use_case.view_networth.ViewNetworthInputBoundary;
import use_case.view_networth.ViewNetworthInputData;

public class ViewNetworthController {
    private ViewNetworthInputBoundary viewNetworthInterator;

    public ViewNetworthController(ViewNetworthInputBoundary viewNetworthInterator) {
        this.viewNetworthInterator = viewNetworthInterator;
    }

    // triggers by the button on the user home page
    public void execute(String userId) {
        viewNetworthInterator.execute(new ViewNetworthInputData(userId));
    }
}

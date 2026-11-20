package interface_adapter;

import use_case.ViewStockListInputBoundary;

public class ViewStockListController {

    private final ViewStockListInputBoundary interactor;

    public ViewStockListController(
            ViewStockListInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute() {
        interactor.execute();
    }
}

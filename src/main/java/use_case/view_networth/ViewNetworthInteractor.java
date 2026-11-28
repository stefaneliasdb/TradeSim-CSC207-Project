package use_case.view_networth;

import entity.Holding;
import entity.Portfolio;
import entity.User;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewNetworthInteractor implements ViewNetworthInputBoundary {
    // pretend is saved.
    private ViewNetworthOutputBoundary viewNetworthPresenter;
    private ViewNetworthDataAccessInterface viewNetworthDataAccessObject;

    public ViewNetworthInteractor(ViewNetworthOutputBoundary viewNetworthPresenter,
                                  ViewNetworthDataAccessInterface viewNetworthDataAccessObject) {
        this.viewNetworthDataAccessObject = viewNetworthDataAccessObject;
        this.viewNetworthPresenter = viewNetworthPresenter;
    }

    @Override
    public void execute(ViewNetworthInputData viewNetworthInputData) {
        Map<LocalTime, BigDecimal> networthHistory = viewNetworthDataAccessObject.getHistory(
                viewNetworthInputData.getUserId()
        );
        viewNetworthPresenter.prepareNetworthView(
                new ViewNetworthOutputData(networthHistory));
    }
}

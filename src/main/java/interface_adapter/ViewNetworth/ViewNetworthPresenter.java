package interface_adapter.ViewNetworth;

import use_case.view_networth.ViewNetworthOutputBoundary;
import use_case.view_networth.ViewNetworthOutputData;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Map;

public class ViewNetworthPresenter implements ViewNetworthOutputBoundary {
    private ViewNetworthViewModel viewNetWorthViewModel;

    public ViewNetworthPresenter(ViewNetworthViewModel viewNetWorthViewModel) {
        this.viewNetWorthViewModel = viewNetWorthViewModel;
    }

    @Override
    public void prepareNetworthView(ViewNetworthOutputData viewNetworthOutputData) {
        Map<LocalTime, BigDecimal> networthHistory = viewNetworthOutputData.getNetworthHistory();
        viewNetWorthViewModel.setNetworthHistory(networthHistory);
        viewNetWorthViewModel.firePropertyChanged();
    }
}

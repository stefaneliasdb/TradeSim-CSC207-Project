package interface_adapter.ViewNetworth;

import use_case.view_networth.ViewNetworthOutputData;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Map;

public class ViewNetworthPresenter {
    private ViewNetWorthViewModel viewNetWorthViewModel;

    public ViewNetworthPresenter(ViewNetWorthViewModel viewNetWorthViewModel) {
        this.viewNetWorthViewModel = viewNetWorthViewModel;
    }

    public void prepareNetworthView(ViewNetworthOutputData viewNetworthOutputData) {
        Map<LocalTime, BigDecimal> networthHistory = viewNetworthOutputData.getNetworthHistory();
        viewNetWorthViewModel.setState();
    }
}

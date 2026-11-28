package interface_adapter.ViewNetworth;

import view.UserHomeView;

import java.math.BigDecimal;
import java.util.List;

public class UserHomeViewModel {
    private UserHomeViewState userHomeViewState = new UserHomeViewState();
    private List<UserHomeViewModelListener> Listeners;

    public void setState(String userName, BigDecimal networth) {

    }

    public void fireStateChanged() {
        for (UserHomeViewModelListener listener : Listeners) {
            listener.notifyStateChanged();
        }
    }

    public void addListener(UserHomeViewModelListener listener) {
        Listeners.add(listener);
    }
}

package view;

import interface_adapter.order.OrderViewModel;
import interface_adapter.order.buy.BuyOrderController;
import interface_adapter.order.sell.SellOrderController;

import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Shared Swing view scaffold for buying and selling shares.
 *
 * <p>The application will supply session::getUserID as currentUserId and
 * portfolioController::viewPortfolio as refreshPortfolio. These callbacks keep
 * teammate classes out of this standalone Order scaffold.
 */
@SuppressWarnings("serial") // This view is not a supported serialized data format.
public final class OrderView extends JPanel implements ActionListener, PropertyChangeListener {
    private static final long serialVersionUID = 1L;

    private final BuyOrderController buyController;
    private final SellOrderController sellController;
    private final OrderViewModel viewModel;
    private final Supplier<String> currentUserId;
    private final Consumer<String> refreshPortfolio;
    private String lastRefreshedOrderID;

    public OrderView(BuyOrderController buyController, SellOrderController sellController,
                     OrderViewModel viewModel, Supplier<String> currentUserId,
                     Consumer<String> refreshPortfolio) {
        this.buyController = Objects.requireNonNull(buyController);
        this.sellController = Objects.requireNonNull(sellController);
        this.viewModel = Objects.requireNonNull(viewModel);
        this.currentUserId = Objects.requireNonNull(currentUserId);
        this.refreshPortfolio = Objects.requireNonNull(refreshPortfolio);
        this.viewModel.addPropertyChangeListener(this);
        // TODO: Build and lay out the symbol/quantity fields and BUY/SELL controls.
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        // TODO: Read currentUserId.get(), parse the UI values, and call the selected controller.
        throw new UnsupportedOperationException("TODO: connect the order UI actions.");
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // TODO: Render the display state.
        // TODO: Refresh Portfolio once for each new FILLED orderID; remember lastRefreshedOrderID.
        // A refresh failure must not reclassify an already committed trade as rejected.
        throw new UnsupportedOperationException("TODO: render and refresh the order view.");
    }
}


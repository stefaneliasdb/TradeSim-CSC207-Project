package view;

import interface_adapter.order.OrderState;
import interface_adapter.order.OrderViewModel;
import interface_adapter.order.buy.BuyOrderController;
import interface_adapter.order.sell.SellOrderController;
import use_case.order.OrderStatus;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Trading controls; session and portfolio callbacks are supplied by the application. */
@SuppressWarnings("serial")
public final class OrderView extends JPanel implements ActionListener, PropertyChangeListener {
    private static final long serialVersionUID = 1L;

    private final BuyOrderController buyController;
    private final SellOrderController sellController;
    private final OrderViewModel viewModel;
    private final Supplier<String> currentUserId;
    private final Consumer<String> refreshPortfolio;
    private final JTextField symbolField = new JTextField(12);
    private final JTextField quantityField = new JTextField("1", 8);
    private final JButton buyButton = new JButton("BUY");
    private final JButton sellButton = new JButton("SELL");
    private final JTextArea message = new JTextArea(2, 28);
    private final JLabel orderID = new JLabel();
    private final JLabel total = new JLabel();
    private final JTextArea notice = new JTextArea(2, 28);
    private final Set<String> refreshedOrderIDs = new HashSet<>();
    private volatile String requestUserID;
    private boolean busy;

    public OrderView(BuyOrderController buyController, SellOrderController sellController,
                     OrderViewModel viewModel, Supplier<String> currentUserId,
                     Consumer<String> refreshPortfolio) {
        this.buyController = Objects.requireNonNull(buyController);
        this.sellController = Objects.requireNonNull(sellController);
        this.viewModel = Objects.requireNonNull(viewModel);
        this.currentUserId = Objects.requireNonNull(currentUserId);
        this.refreshPortfolio = Objects.requireNonNull(refreshPortfolio);
        buildControls();
        this.viewModel.addPropertyChangeListener(this);
        render(viewModel.getState(), null);
    }

    private void buildControls() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createTitledBorder("Place a market order"));
        symbolField.setName("orderSymbol");
        quantityField.setName("orderQuantity");
        buyButton.setName("buyOrder");
        sellButton.setName("sellOrder");
        message.setName("orderMessage");
        orderID.setName("orderID");
        total.setName("orderTotal");
        notice.setName("orderNotice");
        final JPanel form = new JPanel(new GridBagLayout());
        addField(form, "Stock symbol", symbolField, 0);
        addField(form, "Quantity (whole shares)", quantityField, 1);
        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(buyButton);
        buttons.add(sellButton);
        buyButton.addActionListener(this);
        sellButton.addActionListener(this);
        final JPanel entry = new JPanel(new BorderLayout());
        entry.add(form, BorderLayout.CENTER);
        entry.add(buttons, BorderLayout.SOUTH);
        add(entry, BorderLayout.NORTH);

        final JPanel confirmation = new JPanel(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(3, 8, 3, 8);
        configureText(message);
        configureText(notice);
        notice.setForeground(new Color(120, 70, 0));
        constraints.gridy = 0;
        confirmation.add(message, constraints);
        constraints.gridy = 1;
        confirmation.add(total, constraints);
        constraints.gridy = 2;
        confirmation.add(orderID, constraints);
        constraints.gridy = 3;
        confirmation.add(notice, constraints);
        add(confirmation, BorderLayout.CENTER);
    }

    private static void configureText(JTextArea text) {
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setOpaque(false);
    }

    private static void addField(JPanel form, String text, JTextField field, int row) {
        final JLabel label = new JLabel(text);
        label.setLabelFor(field);
        field.getAccessibleContext().setAccessibleName(text);
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = row;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(4, 8, 4, 8);
        form.add(label, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        form.add(field, constraints);
    }

    /** Allows the stock-list selection to populate an order without coupling to its view. */
    public void setSymbol(String symbol) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setSymbol(symbol));
            return;
        }
        if (!busy) {
            symbolField.setText(symbol == null ? "" : symbol);
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> actionPerformed(event));
            return;
        }
        final boolean buying = event.getSource() == buyButton;
        if (busy || (!buying && event.getSource() != sellButton)) {
            return;
        }
        final String userID;
        try {
            userID = currentUserId.get();
        }
        catch (RuntimeException exception) {
            notice.setText("Unable to read the current account. Sign in again before placing an order.");
            return;
        }
        final String symbol = symbolField.getText();
        final int quantity = parseQuantity(quantityField.getText());
        requestUserID = userID;
        notice.setText("Submitting " + (buying ? "buy" : "sell") + " order...");
        setBusy(true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                if (buying) {
                    buyController.placeOrder(userID, symbol, quantity);
                }
                else {
                    sellController.placeOrder(userID, symbol, quantity);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    if (notice.getText().startsWith("Submitting ")) {
                        notice.setText("");
                    }
                }
                catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showUncertainResult();
                }
                catch (ExecutionException | CancellationException exception) {
                    showUncertainResult();
                }
                finally {
                    requestUserID = null;
                    setBusy(false);
                }
            }
        }.execute();
    }

    private static int parseQuantity(String text) {
        try {
            return Integer.parseInt(text.strip());
        }
        catch (NumberFormatException exception) {
            // Invalid UI text becomes an invalid request; the use case owns the validation rule.
            return 0;
        }
    }

    private void setBusy(boolean value) {
        busy = value;
        buyButton.setEnabled(!value);
        sellButton.setEnabled(!value);
        symbolField.setEnabled(!value);
        quantityField.setEnabled(!value);
    }

    private void showUncertainResult() {
        notice.setText("Unable to finish displaying the order. Check your account before trying again.");
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (!"state".equals(event.getPropertyName()) || !(event.getNewValue() instanceof OrderState state)) {
            return;
        }
        final String userID = requestUserID;
        if (SwingUtilities.isEventDispatchThread()) {
            render(state, userID);
        }
        else {
            SwingUtilities.invokeLater(() -> render(state, userID));
        }
    }

    private void render(OrderState state, String submittedUserID) {
        message.setText(state.message());
        message.setForeground(state.status() == OrderStatus.FILLED
                ? new Color(0, 100, 40) : new Color(150, 30, 30));
        total.setText(state.totalText().isEmpty() ? "" : "Trade value: " + state.totalText());
        orderID.setText(state.orderID() == null ? "" : "Order: " + state.orderID());
        if (state.status() == OrderStatus.FILLED && refreshedOrderIDs.add(state.orderID())) {
            try {
                refreshPortfolio.accept(submittedUserID == null ? currentUserId.get() : submittedUserID);
            }
            catch (RuntimeException exception) {
                notice.setText("Order filled, but the account display could not refresh. Reopen the account to verify it.");
            }
        }
    }
}

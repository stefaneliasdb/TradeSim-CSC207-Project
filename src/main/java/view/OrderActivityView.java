package view;

import interface_adapter.order.OrderActivityState;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/** Read-only feedback for trades in the standalone local practice account. */
public final class OrderActivityView extends JPanel {
    private final JLabel cash = new JLabel("Loading practice account...");
    private final JLabel holdings = new JLabel(" ");
    private final JLabel notice = new JLabel(" ");
    private final JTextArea history = new JTextArea(9, 42);

    public OrderActivityView() {
        super(new BorderLayout(6, 6));
        setBorder(BorderFactory.createTitledBorder("Local practice account · USD"));
        JPanel balances = new JPanel(new GridLayout(0, 1, 2, 4));
        cash.setName("practiceCash");
        holdings.setName("practiceHoldings");
        balances.add(cash);
        balances.add(holdings);
        add(balances, BorderLayout.NORTH);
        history.setName("orderHistory");
        history.setEditable(false);
        history.setLineWrap(true);
        history.setWrapStyleWord(true);
        history.getAccessibleContext().setAccessibleName("Executed order history");
        add(new JScrollPane(history), BorderLayout.CENTER);
        add(notice, BorderLayout.SOUTH);
    }

    public void display(OrderActivityState state) {
        cash.setText(state.cashText());
        holdings.setText(state.holdingsText());
        history.setText(state.trades().isEmpty() ? "No executed orders yet."
                : String.join("\n", state.trades()));
        history.setCaretPosition(0);
        notice.setText("Saved locally; account data is kept between runs.");
    }

    public void showRefreshError() {
        notice.setText("Account display unavailable. Check saved trades before retrying an order.");
    }
}

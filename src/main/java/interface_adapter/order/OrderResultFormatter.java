package interface_adapter.order;

import use_case.order.OrderOutputData;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

/** Shared display wording and USD formatting; it does not decide whether a trade is allowed. */
public final class OrderResultFormatter {
    private OrderResultFormatter() {
    }

    public static void present(OrderViewModel viewModel, OrderOutputData result, String action) {
        Objects.requireNonNull(result);
        final String detail = switch (result.getStatus()) {
            case FILLED -> "order filled.";
            case INVALID_REQUEST -> "order rejected: enter a stock symbol and a positive whole-number quantity"
                    + " while signed in.";
            case ACCOUNT_NOT_FOUND -> "order rejected: no trading account was found for this session.";
            case QUOTE_UNAVAILABLE -> "order rejected: a valid USD market quote is unavailable.";
            case INSUFFICIENT_CASH -> "order rejected: not enough available cash to buy these shares.";
            case INSUFFICIENT_SHARES -> "order rejected: not enough shares are available to sell.";
            case HOLDING_LIMIT_EXCEEDED -> "order rejected: the supported share quantity would be exceeded.";
            case ACCOUNT_CHANGED -> "order rejected: your account changed. Review your balance and try again.";
            case SERVICE_UNAVAILABLE -> "order rejected: the account service is unavailable. Please try again.";
        };
        final String total = result.isSuccess()
                ? NumberFormat.getCurrencyInstance(Locale.US).format(result.getTotalValue()) : "";
        viewModel.update(result.getStatus(), action + " " + detail, result.getOrderID(), total);
    }
}

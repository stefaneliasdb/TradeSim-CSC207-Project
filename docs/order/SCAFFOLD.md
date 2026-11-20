# Order implementation scaffold

This is the class-file setup for two complete use cases: **Buy Shares** and **Sell Shares**.
Trading execution, UI event handling, result formatting, and account persistence are intentionally
left as explicit `TODO` methods that throw `UnsupportedOperationException`. They do not simulate
success or silently change an account. The application and tests were not run for this setup.

## Current structure

```text
src/main/java/
  entity/
    Order.java                    existing completed-trade entity, unchanged
    OrderType.java                existing BUY / SELL enum, unchanged
  use_case/order/
    PlaceOrderInputData.java      userID, symbol, quantity; no request-side direction
    OrderOutputData.java          status, optional-on-rejection orderID and totalValue
    OrderAccountSnapshot.java     cash and one symbol's quantity
    OrderStatus.java
    OrderQuoteGateway.java
    OrderAccountGateway.java
    buy/
      BuyOrderInputBoundary.java
      BuyOrderInteractor.java
      BuyOrderOutputBoundary.java
    sell/
      SellOrderInputBoundary.java
      SellOrderInteractor.java
      SellOrderOutputBoundary.java
  interface_adapter/order/
    OrderViewModel.java
    buy/
      BuyOrderController.java
      BuyOrderPresenter.java
    sell/
      SellOrderController.java
      SellOrderPresenter.java
  view/
    OrderView.java
  data_access/order/
    OrderAccountAdapter.java
```

Constructors, dependency fields, interfaces, DTOs and result getters are available. Existing entity,
snapshot and enum behavior is retained. `OrderView` registers itself as a ViewModel listener during
construction. The ViewModel's listener registration and result-object factories are setup/data
boilerplate, not a completed trading implementation.

## Implementation work remaining

1. Implement each interactor's request checks, quote/account read, side-specific calculations,
   candidate Order creation, atomic commit and exactly one business-result output.
2. Connect each controller to its input boundary using `PlaceOrderInputData`.
3. Implement each presenter: map status to text, format amounts and update the shared ViewModel.
4. Implement `OrderViewModel.update`: store the state, then send one property-change notification.
5. Build the Swing controls and implement the View handlers. Refresh Portfolio once per new
   successful order ID; a refresh failure must not reclassify a committed trade as rejected.
6. Wire `OrderAccountAdapter` to the team's actual account store, then verify its all-or-nothing
   commit contract with integration/contract tests. The current adapter performs no storage work.

## Team integration without placeholder teammate classes

The application composition root will inject the existing team's shared quote adapter through
`OrderQuoteGateway`. No replacement Stock, User, Portfolio or team DAO is created here.

`OrderView` accepts `Supplier<String> currentUserId` and `Consumer<String> refreshPortfolio`.
Once the team modules are available, wire them with `session::getUserID` and
`portfolioController::viewPortfolio`. These are future wiring expressions, not claims that the
corresponding teammate classes are already implemented in this checkout.

`OrderAccountAdapter` deliberately has no fake DAO field. Its future constructor dependency must
match the actual team storage API. Cash, target holdings and appended history must commit together,
with account-wide concurrency coordination; `findById` and `save` signatures alone do not establish
that guarantee. A live mutable User must not leak partially applied changes.

## UML files

The current split-use-case PlantUML sources and rendered diagrams are in `docs/order/uml/`.

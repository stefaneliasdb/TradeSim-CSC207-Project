package use_case;

import entity.Stock;
import java.util.Optional;

public interface ViewStockListDataAccessInterface {

    Optional<Stock> findStock(String symbol);

}
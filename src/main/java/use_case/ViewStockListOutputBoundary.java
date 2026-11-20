package use_case;

public interface ViewStockListOutputBoundary {

    void prepareSuccessView(ViewStockListOutputData outputData);

    void prepareFailView(String errorMessage);

}
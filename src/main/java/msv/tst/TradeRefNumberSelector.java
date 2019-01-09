package msv.tst;

public interface TradeRefNumberSelector {

    String developerSide(boolean isFirstOption);

    String serverSide(boolean isFirstOption);
}

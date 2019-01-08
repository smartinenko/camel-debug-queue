package msv.tst;

public interface TradeRefNumberSelector {

    String includeTradePrefix(boolean isFirstOption);

    String excludeTradePrefix(boolean isFirstOption);
}

package msv.tst;

public interface TradeRefNumberSelector {
    String PROPERTY_DEV_PREFIX_DELIMITER = ";";
    String MCP_SYSTEM_HEADER = "MCP_SYSTEM_HEADER";
    String JMS_TRADE_REF_NBR_INCLUDE = "JMS_TRADE_REF_NBR_INCLUDE";
    String JMS_TRADE_REF_NBR_EXCLUDE = "JMS_TRADE_REF_NBR_EXCLUDE";

    String include(boolean isFirstOption);
    String exclude(boolean isFirstOption);
}

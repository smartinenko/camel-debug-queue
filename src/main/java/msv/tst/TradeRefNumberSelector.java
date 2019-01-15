package msv.tst;

/**
 * Generates JMS selector string which could be used as an option in connection URI
 */
public interface TradeRefNumberSelector {

    /**
     * Generates option JMS selector which excludes tradeRefNbr prefixes on server side
     * @param isFirstOption
     *        is JMS selector option first in the option's list
     * @return option JMS selector
     *         example: ?selector=MCP_SYSTEM_HEADER NOT LIKE '%tradeRefNbr":"LOCAL%'
     *                  AND MCP_SYSTEM_HEADER NOT LIKE '%tradeRefNbr":"LOCAL2%'
     */
    String serverSide(boolean isFirstOption);

    /**
     * Generates option JMS selector which includes tradeRefNbr prefixes on local side
     * @param isFirstOption
     *        is JMS selector option first in the option's list
     * @return option JMS selector
     *         example: ?selector=MCP_SYSTEM_HEADER LIKE '%tradeRefNbr":"LOCAL%'
     *                  OR MCP_SYSTEM_HEADER LIKE '%tradeRefNbr":"LOCAL2%'
     */
    String developerSide(boolean isFirstOption);
}

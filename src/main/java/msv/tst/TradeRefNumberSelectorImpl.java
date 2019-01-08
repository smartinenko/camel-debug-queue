package msv.tst;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.EMPTY;

@Component
@Slf4j
public class TradeRefNumberSelectorImpl implements TradeRefNumberSelector {

    static final String PROPERTY_DEV_PREFIX_DELIMITER = ";";
    static final String MCP_SYSTEM_HEADER = "MCP_SYSTEM_HEADER";
    static final String JMS_TRADE_REF_NBR_PREFIX = "JMS_TRADE_REF_NBR_PREFIX";

    @Override
    public String includeTradePrefix(boolean isFirstOption) {
        String prefixValue = System.getenv(JMS_TRADE_REF_NBR_PREFIX);
        if (isNull(prefixValue)) {
            log.info("Environment variable JMS_TRADE_REF_NBR_PREFIX is not set, no JMSSelector generated");
            return EMPTY;
        }

        String parameterDelimiter = isFirstOption ? "?" : "&";
        String likeStatement = buildLikeStatement(prefixValue);
        if (StringUtils.isBlank(likeStatement)) {
            log.info("LikeStatement is empty, no JMSSelector generated");
            return EMPTY;
        }
        String jmsSelector = parameterDelimiter + "selector=" + likeStatement;
        log.info("JmsSelector generated: {}", jmsSelector);
        return jmsSelector;
    }

    @Override
    public String excludeTradePrefix(boolean isFirstOption) {
        String prefixValue = System.getenv(JMS_TRADE_REF_NBR_PREFIX);
        if (isNull(prefixValue)) {
            log.info("Environment variable JMS_TRADE_REF_NBR_PREFIX is not set, no JMSSelector generated");
            return EMPTY;
        }

        String parameterDelimiter = isFirstOption ? "?" : "&";
        String notLikeStatement = buildNotLikeStatement(prefixValue);
        if (StringUtils.isBlank(notLikeStatement)) {
            log.info("NotLikeStatement is empty, no JMSSelector generated");
            return EMPTY;
        }
        String jmsSelector = parameterDelimiter + "selector=" + notLikeStatement;
        log.info("JmsSelector generated: {}", jmsSelector);
        return jmsSelector;
    }

    private String buildLikeStatement(final String envProperty) {
        return Arrays.stream(envProperty.split(PROPERTY_DEV_PREFIX_DELIMITER))
                .filter(StringUtils::isNotBlank)
                .map(s -> MCP_SYSTEM_HEADER + " LIKE '%tradeRefNbr\":\"" + s + "%'")
                .collect(Collectors.joining(" OR "));
    }

    private String buildNotLikeStatement(final String envProperty) {
        return Arrays.stream(envProperty.split(PROPERTY_DEV_PREFIX_DELIMITER))
                .filter(StringUtils::isNotBlank)
                .map(s -> MCP_SYSTEM_HEADER + " NOT LIKE '%tradeRefNbr\":\"" + s + "%'")
                .collect(Collectors.joining(" AND "));
    }
}

package msv.tst;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Component
public class TradeRefNumberSelectorImpl implements TradeRefNumberSelector, ApplicationContextAware {

    private ApplicationContext appContext;

    @Override
    public String include(boolean isFirstOption) {
        Environment environment= appContext.getEnvironment();
        final String JMS_DEBUG_SELECTOR = environment.getProperty(JMS_TRADE_REF_NBR_INCLUDE);
        if (isNull(JMS_DEBUG_SELECTOR)) {
            return "";
        }

        final String PREFIX = isFirstOption ? "?" : "&";
        final String likeStatement = buildLikeStatement(JMS_DEBUG_SELECTOR);
        return StringUtils.isBlank(likeStatement) ? "" : PREFIX + "selector=" + likeStatement;
    }

    @Override
    public String exclude(boolean isFirstOption) {
        Environment environment= appContext.getEnvironment();
        final String JMS_DEBUG_SELECTOR = environment.getProperty(JMS_TRADE_REF_NBR_EXCLUDE);
        if (isNull(JMS_DEBUG_SELECTOR)) {
            return "";
        }

        final String PREFIX = isFirstOption ? "?" : "&";
        final String notLikeStatement = buildNotLikeStatement(JMS_DEBUG_SELECTOR);
        return StringUtils.isBlank(notLikeStatement) ? "" : PREFIX + "selector=" + notLikeStatement;
    }

    public static void main(String[] args) {
        TradeRefNumberSelectorImpl t = new TradeRefNumberSelectorImpl();
        System.out.println("=" + t.buildLikeStatement("MSV;JUL"));
        System.out.println("=<" + t.buildLikeStatement("") + ">");

        System.out.println("=" + t.buildNotLikeStatement("MSV;JUL"));
        System.out.println("=<" + t.buildNotLikeStatement("") + ">");
    }

    protected String buildLikeStatement(final String envProperty) {
        return Arrays.stream(envProperty.split(PROPERTY_DEV_PREFIX_DELIMITER))
                .filter(StringUtils::isNotBlank)
                .map(s -> MCP_SYSTEM_HEADER + " LIKE '%tradeRefNbr\":\"" + s + "%'")
                .collect(Collectors.joining(" OR "));
    }

    protected String buildNotLikeStatement(final String envProperty) {
        return Arrays.stream(envProperty.split(PROPERTY_DEV_PREFIX_DELIMITER))
                .filter(StringUtils::isNotBlank)
                .map(s -> MCP_SYSTEM_HEADER + " NOT LIKE '%tradeRefNbr\":\"" + s + "%'")
                .collect(Collectors.joining(" AND "));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }
}

package msv.tst;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import static java.util.Objects.isNull;
import static msv.tst.MainCamel.MCP_SYSTEM_HEADER;
import static msv.tst.MainCamel.TOPIC;

public class MainRouteBuilder extends RouteBuilder implements ApplicationContextAware {

    private ApplicationContext appContext;

    @Override
    public void configure() throws Exception {

        Environment environment= appContext.getEnvironment();
        final String JMS_DEBUG_SELECTOR = environment.getProperty("JMS_DEBUG_SELECTOR");
        final String SELECTOR = isNull(JMS_DEBUG_SELECTOR) ? "" : JMS_DEBUG_SELECTOR;

        from(TOPIC + SELECTOR)
                .routeId("dev-route")
                .process(e ->
                    System.out.println(e.getFromRouteId() +
                            " HEADER= " + e.getIn().getHeader(MCP_SYSTEM_HEADER) +
                            " body= " + e.getIn().getBody())
                )
                .end();

        from(TOPIC + "?selector=MCP_SYSTEM_HEADER LIKE '%tradeRefNbr\":\"JM001%'")
                .routeId("debug-route")
                .process(e ->
                    System.out.println(e.getFromRouteId() +
                            " HEADER= " + e.getIn().getHeader(MCP_SYSTEM_HEADER) +
                            " body= " + e.getIn().getBody())
                )
                .end();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }
}
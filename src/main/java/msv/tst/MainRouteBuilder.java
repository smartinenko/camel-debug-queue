package msv.tst;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static msv.tst.TradeRefNumberSelector.MCP_SYSTEM_HEADER;

@Component
public class MainRouteBuilder extends RouteBuilder {
    public static final String TOPIC = "activemq:topic:Consumer.VirtualTopic.1111";

    @Autowired
    private TradeRefNumberSelector tradeRefNumberSelector;

    @Override
    public void configure() throws Exception {

        final String SELECTOR = tradeRefNumberSelector.exclude(true);
        //activemq:topic:topic.name?selector=MCP_SYSTEM_HEADER NOT LIKE '%tradeRefNbr":"JM001%'

        from(TOPIC + SELECTOR)
                .routeId("server-route")
                .process(e ->
                    System.out.println(e.getFromRouteId() +
//                            " HEADER= " + e.getIn().getHeader(MCP_SYSTEM_HEADER) +
                            " body= " + e.getIn().getBody())
                )
                .end();

        final String SELECTOR2 = tradeRefNumberSelector.include(true);
// activemq:topic:topic.name?selector=MCP_SYSTEM_HEADER LIKE '%tradeRefNbr":"JM001%' OR MCP_SYSTEM_HEADER LIKE '%tradeRefNbr":"DEV%'
        from(TOPIC + SELECTOR2)
//        from(TOPIC + "?selector=MCP_SYSTEM_HEADER LIKE '%tradeRefNbr\":\"JM001%'")
                .routeId("host-route")
                .process(e ->
                    System.out.println(e.getFromRouteId() +
//                            " HEADER= " + e.getIn().getHeader(MCP_SYSTEM_HEADER) +
                            " body= " + e.getIn().getBody())
                )
                .end();
    }
}
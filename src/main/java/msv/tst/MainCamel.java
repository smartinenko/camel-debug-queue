package msv.tst;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MainCamel {
    public static final String TOPIC = "activemq:topic:Consumer.VirtualTopic.1111";
    public static final String MCP_SYSTEM_HEADER = "MCP_SYSTEM_HEADER";
    public static final String DEV_MCP_SYSTEM_HEADER = "{\"SystemHeader\":{\"version\":1.0,\"tradeInfo\":{\"activity\":\"NEW\",\"status\":\"CONFIRMED\",\"transType\":\"TRADECONFIRMED\",\"productType\":\"CreditDefaultSwapIndex\",\"assetClass\":\"Credit\",\"submitter\":\"00006666\",\"tradeParty\":\"00006666\",\"tradeRefNbr\":\"DEV_4A3C6C27D958410E80FBC51FF5328D6B2\",\"matchId\":\"MSERV20181113.0034181940\",\"matchIndexId\":\"00005555$$Trade$$DEV_4A3C6C27D958410E80FBC51FF5328D6B2\",\"confirmationTimestamp\":\"2018-11-13T16:14:45.080Z\",\"counterpartyID\":\"00005555\",\"messageType\":\"Trade Registration\",\"messageId\":\"WEB-WEBTCECT-Matthew McClements\",\"registrationState\":\"SentForRegistration\",\"creationTimeStamp\":\"2015-12-10T08:39:57.000-05:00\",\"counterpartyTradeRefNbr\":\"DEV_4A3C6C27D958410E80FBC51FF5328D6B2\",\"businessService\":\"Unknown\"},\"routingInfo\":{\"fpmlVersion\":\"0409\",\"mtcVersion\":\"1100\",\"rmVersion\":\"1100\",\"route\":\"1100.0409\",\"sourceMQLabel\":\"A3.d3confirms.IN.SL1\"},\"internalInfo\":{\"rawInPersistenceId\":\"2058461\",\"tradeSideId\":0,\"eventSideId\":0}}}";
    public static final String DEBUG_MCP_SYSTEM_HEADER = "{\"SystemHeader\":{\"version\":1.0,\"tradeInfo\":{\"activity\":\"NEW\",\"status\":\"CONFIRMED\",\"transType\":\"TRADECONFIRMED\",\"productType\":\"CreditDefaultSwapIndex\",\"assetClass\":\"Credit\",\"submitter\":\"00006666\",\"tradeParty\":\"00006666\",\"tradeRefNbr\":\"JM001_4A3C6C27D958410E80FBC51FF5328D6B2\",\"matchId\":\"MSERV20181113.0034181940\",\"matchIndexId\":\"00005555$$Trade$$JM001_4A3C6C27D958410E80FBC51FF5328D6B1\",\"confirmationTimestamp\":\"2018-11-13T16:14:45.080Z\",\"counterpartyID\":\"00005555\",\"messageType\":\"Trade Registration\",\"messageId\":\"WEB-WEBTCECT-Matthew McClements\",\"registrationState\":\"SentForRegistration\",\"creationTimeStamp\":\"2015-12-10T08:39:57.000-05:00\",\"counterpartyTradeRefNbr\":\"JM001_4A3C6C27D958410E80FBC51FF5328D6B2\",\"businessService\":\"Unknown\"},\"routingInfo\":{\"fpmlVersion\":\"0409\",\"mtcVersion\":\"1100\",\"rmVersion\":\"1100\",\"route\":\"1100.0409\",\"sourceMQLabel\":\"A3.d3confirms.IN.SL1\"},\"internalInfo\":{\"rawInPersistenceId\":\"2058461\",\"tradeSideId\":0,\"eventSideId\":0}}}";

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        CamelContext camelContext = SpringCamelContext.springCamelContext(appContext, false);

        JmsComponent jms = (JmsComponent)appContext.getBean("jms");
        MainRouteBuilder mainRouteBuilder = (MainRouteBuilder)appContext.getBean("mainRouteBuilder");

        try {
            camelContext.addComponent("activemq", jms);
            camelContext.addRoutes(mainRouteBuilder);

            ProducerTemplate template = camelContext.createProducerTemplate();

            String headerValue;
            String body;
            boolean isDebug = false;

            for (int i=0; i<4; i++) {
                if (isDebug) {
                    headerValue = DEV_MCP_SYSTEM_HEADER;
                    body = "{DEBUG BODY}";
                } else {
                    headerValue = DEBUG_MCP_SYSTEM_HEADER;
                    body = "{DEV BODY}";
                }
                isDebug = !isDebug;

                template.sendBodyAndHeader(TOPIC, body, MCP_SYSTEM_HEADER, headerValue);
            }

            Thread.sleep(2_000);
        } finally {
            camelContext.stop();
            System.exit(0);
        }
    }
}

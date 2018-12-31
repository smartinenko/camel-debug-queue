package msv.tst;

import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spring.SpringCamelContext;
import org.junit.Test;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertEquals;
import static msv.tst.TradeRefNumberSelector.MCP_SYSTEM_HEADER;

public class ContextManagerTest {
    public static final String TOPIC = "activemq:topic:Consumer.VirtualTopic.1111";
    public static final String REGULAR_SYSTEM_HEADER = "{\"SystemHeader\":{\"version\":1.0,\"tradeInfo\":{\"activity\":\"NEW\",\"status\":\"CONFIRMED\",\"transType\":\"TRADECONFIRMED\",\"productType\":\"CreditDefaultSwapIndex\",\"assetClass\":\"Credit\",\"submitter\":\"00006666\",\"tradeParty\":\"00006666\",\"tradeRefNbr\":\"REGULAR_4A3C6C27D958410E80FBC51FF5328D6B2\",\"matchId\":\"MSERV20181113.0034181940\",\"matchIndexId\":\"00005555$$Trade$$REGULAR_4A3C6C27D958410E80FBC51FF5328D6B2\",\"confirmationTimestamp\":\"2018-11-13T16:14:45.080Z\",\"counterpartyID\":\"00005555\",\"messageType\":\"Trade Registration\",\"messageId\":\"WEB-WEBTCECT-Matthew McClements\",\"registrationState\":\"SentForRegistration\",\"creationTimeStamp\":\"2015-12-10T08:39:57.000-05:00\",\"counterpartyTradeRefNbr\":\"REGULAR_4A3C6C27D958410E80FBC51FF5328D6B2\",\"businessService\":\"Unknown\"},\"routingInfo\":{\"fpmlVersion\":\"0409\",\"mtcVersion\":\"1100\",\"rmVersion\":\"1100\",\"route\":\"1100.0409\",\"sourceMQLabel\":\"A3.d3confirms.IN.SL1\"},\"internalInfo\":{\"rawInPersistenceId\":\"2058461\",\"tradeSideId\":0,\"eventSideId\":0}}}";
    public static final String SPECIAL_SYSTEM_HEADER = "{\"SystemHeader\":{\"version\":1.0,\"tradeInfo\":{\"activity\":\"NEW\",\"status\":\"CONFIRMED\",\"transType\":\"TRADECONFIRMED\",\"productType\":\"CreditDefaultSwapIndex\",\"assetClass\":\"Credit\",\"submitter\":\"00006666\",\"tradeParty\":\"00006666\",\"tradeRefNbr\":\"SPECIAL_4A3C6C27D958410E80FBC51FF5328D6B2\",\"matchId\":\"MSERV20181113.0034181940\",\"matchIndexId\":\"00005555$$Trade$$SPECIAL_4A3C6C27D958410E80FBC51FF5328D6B1\",\"confirmationTimestamp\":\"2018-11-13T16:14:45.080Z\",\"counterpartyID\":\"00005555\",\"messageType\":\"Trade Registration\",\"messageId\":\"WEB-WEBTCECT-Matthew McClements\",\"registrationState\":\"SentForRegistration\",\"creationTimeStamp\":\"2015-12-10T08:39:57.000-05:00\",\"counterpartyTradeRefNbr\":\"SPECIAL_4A3C6C27D958410E80FBC51FF5328D6B2\",\"businessService\":\"Unknown\"},\"routingInfo\":{\"fpmlVersion\":\"0409\",\"mtcVersion\":\"1100\",\"rmVersion\":\"1100\",\"route\":\"1100.0409\",\"sourceMQLabel\":\"A3.d3confirms.IN.SL1\"},\"internalInfo\":{\"rawInPersistenceId\":\"2058461\",\"tradeSideId\":0,\"eventSideId\":0}}}";

    @Test
    public void send() throws Exception {
        final AtomicInteger serverMessagesCount = new AtomicInteger(0);
        final AtomicInteger devMessagesCount = new AtomicInteger(0);

        ContextManager manager = new ContextManager();
        manager.start();
        SpringCamelContext camelContext = manager.getCamelContext();
        TradeRefNumberSelector tradeRefNumberSelector = (TradeRefNumberSelector)manager.getAppContext().getBean("tradeRefNumberSelectorImpl");


        System.setProperty("JMS_TRADE_REF_NBR_EXCLUDE", "SPECIAL");
//        System.setProperty("JMS_TRADE_REF_NBR_EXCLUDE", "SPECIAL;REGULAR");
        final String EXCLUDE_SELECTOR = tradeRefNumberSelector.exclude(true);
        System.clearProperty("JMS_TRADE_REF_NBR_EXCLUDE");
        Endpoint serverSideEndpint = camelContext.getEndpoint(TOPIC + EXCLUDE_SELECTOR);
        Consumer serverSideConsumer = serverSideEndpint.createConsumer(exchange -> {
            System.out.println("server=" + exchange.getIn().getBody(String.class));
            serverMessagesCount.addAndGet(1);
        });
        serverSideConsumer.start();

        System.setProperty("JMS_TRADE_REF_NBR_INCLUDE", "SPECIAL");
        final String INCLUDE_SELECTOR = tradeRefNumberSelector.include(true);
        System.clearProperty("JMS_TRADE_REF_NBR_INCLUDE");
        Endpoint devSideEndpint = camelContext.getEndpoint(TOPIC + INCLUDE_SELECTOR);
        Consumer devSideConsumer = devSideEndpint.createConsumer(exchange -> {
            System.out.println("dev_host=" + exchange.getIn().getBody(String.class));
            devMessagesCount.addAndGet(1);
        });
        devSideConsumer.start();
        Thread.sleep(100);


        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();

        final int NUMBER_OF_REGULAR_MESSAGES = 10;
        for(int i=0; i<NUMBER_OF_REGULAR_MESSAGES; i++)
            producerTemplate.sendBodyAndHeader(TOPIC, "REGULAR", MCP_SYSTEM_HEADER, REGULAR_SYSTEM_HEADER);

        final int NUMBER_OF_SPECIAL_MESSAGES = 5;
        for(int i=0; i<NUMBER_OF_SPECIAL_MESSAGES; i++)
            producerTemplate.sendBodyAndHeader(TOPIC, "SPECIAL", MCP_SYSTEM_HEADER, SPECIAL_SYSTEM_HEADER);

        serverSideConsumer.stop();
        devSideConsumer.stop();
        manager.stop();

        assertEquals(serverMessagesCount.get(), NUMBER_OF_REGULAR_MESSAGES);
        assertEquals(devMessagesCount.get(), NUMBER_OF_SPECIAL_MESSAGES);
    }

}
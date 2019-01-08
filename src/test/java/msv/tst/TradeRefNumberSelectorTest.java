package msv.tst;

import lombok.extern.slf4j.Slf4j;
import msv.tst.util.ContextManager;
import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spring.SpringCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertEquals;
import static msv.tst.TradeRefNumberSelectorImpl.*;
import static msv.tst.util.TestUtils.removeEnvVariable;
import static msv.tst.util.TestUtils.setEnv;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
@Slf4j
public class TradeRefNumberSelectorTest {
    private static final String TOPIC = "activemq:topic:Consumer.VirtualTopic.1111";
    private static final String SERVER_SYSTEM_HEADER = "{\"SystemHeader\":{\"version\":1.0,\"tradeInfo\":{\"activity\":\"NEW\",\"status\":\"CONFIRMED\",\"transType\":\"TRADECONFIRMED\",\"productType\":\"CreditDefaultSwapIndex\",\"assetClass\":\"Credit\",\"submitter\":\"00006666\",\"tradeParty\":\"00006666\",\"tradeRefNbr\":\"SERVER_4A3C6C27D958410E80FBC51FF5328D6B2\",\"matchId\":\"MSERV20181113.0034181940\",\"matchIndexId\":\"00005555$$Trade$$REGULAR_4A3C6C27D958410E80FBC51FF5328D6B2\",\"confirmationTimestamp\":\"2018-11-13T16:14:45.080Z\",\"counterpartyID\":\"00005555\",\"messageType\":\"Trade Registration\",\"messageId\":\"WEB-WEBTCECT-Matthew McClements\",\"registrationState\":\"SentForRegistration\",\"creationTimeStamp\":\"2015-12-10T08:39:57.000-05:00\",\"counterpartyTradeRefNbr\":\"REGULAR_4A3C6C27D958410E80FBC51FF5328D6B2\",\"businessService\":\"Unknown\"},\"routingInfo\":{\"fpmlVersion\":\"0409\",\"mtcVersion\":\"1100\",\"rmVersion\":\"1100\",\"route\":\"1100.0409\",\"sourceMQLabel\":\"A3.d3confirms.IN.SL1\"},\"internalInfo\":{\"rawInPersistenceId\":\"2058461\",\"tradeSideId\":0,\"eventSideId\":0}}}";
    private static final String LOCAL_SYSTEM_HEADER = "{\"SystemHeader\":{\"version\":1.0,\"tradeInfo\":{\"activity\":\"NEW\",\"status\":\"CONFIRMED\",\"transType\":\"TRADECONFIRMED\",\"productType\":\"CreditDefaultSwapIndex\",\"assetClass\":\"Credit\",\"submitter\":\"00006666\",\"tradeParty\":\"00006666\",\"tradeRefNbr\":\"LOCAL_4A3C6C27D958410E80FBC51FF5328D6B2\",\"matchId\":\"MSERV20181113.0034181940\",\"matchIndexId\":\"00005555$$Trade$$SPECIAL_4A3C6C27D958410E80FBC51FF5328D6B1\",\"confirmationTimestamp\":\"2018-11-13T16:14:45.080Z\",\"counterpartyID\":\"00005555\",\"messageType\":\"Trade Registration\",\"messageId\":\"WEB-WEBTCECT-Matthew McClements\",\"registrationState\":\"SentForRegistration\",\"creationTimeStamp\":\"2015-12-10T08:39:57.000-05:00\",\"counterpartyTradeRefNbr\":\"SPECIAL_4A3C6C27D958410E80FBC51FF5328D6B2\",\"businessService\":\"Unknown\"},\"routingInfo\":{\"fpmlVersion\":\"0409\",\"mtcVersion\":\"1100\",\"rmVersion\":\"1100\",\"route\":\"1100.0409\",\"sourceMQLabel\":\"A3.d3confirms.IN.SL1\"},\"internalInfo\":{\"rawInPersistenceId\":\"2058461\",\"tradeSideId\":0,\"eventSideId\":0}}}";
    private static final String LOCAL2_SYSTEM_HEADER = "{\"SystemHeader\":{\"version\":1.0,\"tradeInfo\":{\"activity\":\"NEW\",\"status\":\"CONFIRMED\",\"transType\":\"TRADECONFIRMED\",\"productType\":\"CreditDefaultSwapIndex\",\"assetClass\":\"Credit\",\"submitter\":\"00006666\",\"tradeParty\":\"00006666\",\"tradeRefNbr\":\"LOCAL2_4A3C6C27D958410E80FBC51FF5328D6B2\",\"matchId\":\"MSERV20181113.0034181940\",\"matchIndexId\":\"00005555$$Trade$$SPECIAL_4A3C6C27D958410E80FBC51FF5328D6B1\",\"confirmationTimestamp\":\"2018-11-13T16:14:45.080Z\",\"counterpartyID\":\"00005555\",\"messageType\":\"Trade Registration\",\"messageId\":\"WEB-WEBTCECT-Matthew McClements\",\"registrationState\":\"SentForRegistration\",\"creationTimeStamp\":\"2015-12-10T08:39:57.000-05:00\",\"counterpartyTradeRefNbr\":\"SPECIAL_4A3C6C27D958410E80FBC51FF5328D6B2\",\"businessService\":\"Unknown\"},\"routingInfo\":{\"fpmlVersion\":\"0409\",\"mtcVersion\":\"1100\",\"rmVersion\":\"1100\",\"route\":\"1100.0409\",\"sourceMQLabel\":\"A3.d3confirms.IN.SL1\"},\"internalInfo\":{\"rawInPersistenceId\":\"2058461\",\"tradeSideId\":0,\"eventSideId\":0}}}";
    private static final String SERVER_PREFIX = "SERVER";
    private static final String LOCAL_PREFIX = "LOCAL";
    private static final String LOCAL2_PREFIX = "LOCAL2";
    private static final int NUMBER_OF_SERVER_MESSAGES = 10;
    private static final int NUMBER_OF_LOCAL_MESSAGES = 5;
    private static final int NUMBER_OF_LOCAL_MESSAGES_MULTIPLE_CHECK = 10;

    @Autowired
    private TradeRefNumberSelector tradeRefNumberSelector;
    private SpringCamelContext camelContext;
    private ContextManager manager = new ContextManager();
    private ProducerTemplate producerTemplate;
    private Map<String, String> envVariables = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        manager.start();
        camelContext = manager.getCamelContext();
        producerTemplate = camelContext.createProducerTemplate();
    }

    @After
    public void shutDown() throws Exception {
        manager.stop();
        removeEnvVariable(JMS_TRADE_REF_NBR_PREFIX);
    }

    @Test
    public void testServerSideExcludeSingleTrade() throws Exception {
        AtomicInteger serverMessagesCount = new AtomicInteger(0);
        envVariables.put(JMS_TRADE_REF_NBR_PREFIX, LOCAL_PREFIX);
        setEnv(envVariables);
        String excludeSelector = tradeRefNumberSelector.excludeTradePrefix(true);
        Endpoint serverSideEndpoint = camelContext.getEndpoint(TOPIC + excludeSelector);
        Consumer serverSideConsumer = serverSideEndpoint.createConsumer(exchange -> {
            log.info("server_host = {}", exchange.getIn().getBody(String.class));
            serverMessagesCount.addAndGet(1);
        });
        serverSideConsumer.start();

        sendServerAndLocalMessages();
        Thread.sleep(10);
        serverSideConsumer.stop();

        assertEquals(NUMBER_OF_SERVER_MESSAGES, serverMessagesCount.get());
    }

    @Test
    public void testLocalSideIncludeSingleTrade() throws Exception {
        AtomicInteger localMessagesCount = new AtomicInteger(0);
        Map<String, String> envVariable = new HashMap<>();
        envVariable.put(JMS_TRADE_REF_NBR_PREFIX, LOCAL_PREFIX);
        setEnv(envVariable);
        String includeSelector = tradeRefNumberSelector.includeTradePrefix(true);
        Endpoint localSideEndpoint = camelContext.getEndpoint(TOPIC + includeSelector);
        Consumer localSideConsumer = localSideEndpoint.createConsumer(exchange -> {
            log.info("local_host = {}", exchange.getIn().getBody(String.class));
            localMessagesCount.addAndGet(1);
        });
        localSideConsumer.start();

        sendServerAndLocalMessages();
        Thread.sleep(10);
        localSideConsumer.stop();

        assertEquals(NUMBER_OF_LOCAL_MESSAGES, localMessagesCount.get());
    }

    @Test
    public void testServerSideExcludeMultipleTrade() throws Exception {
        AtomicInteger serverMessagesCount = new AtomicInteger(0);
        Map<String, String> envVariable = new HashMap<>();
        envVariable.put(JMS_TRADE_REF_NBR_PREFIX, LOCAL_PREFIX + PROPERTY_DEV_PREFIX_DELIMITER + LOCAL2_PREFIX);
        setEnv(envVariable);
        String excludeSelector = tradeRefNumberSelector.excludeTradePrefix(true);
        Endpoint serverSideEndpoint = camelContext.getEndpoint(TOPIC + excludeSelector);
        Consumer serverSideConsumer = serverSideEndpoint.createConsumer(exchange -> {
            log.info("server_host = {}", exchange.getIn().getBody(String.class));
            serverMessagesCount.addAndGet(1);
        });
        serverSideConsumer.start();

        sendServerAndLocalMessages();
        sendLocalMessages();
        Thread.sleep(10);
        serverSideConsumer.stop();

        assertEquals(NUMBER_OF_SERVER_MESSAGES, serverMessagesCount.get());
    }

    @Test
    public void testLocalSideIncludeMultipleTrade() throws Exception {
        AtomicInteger localMessagesCount = new AtomicInteger(0);
        Map<String, String> envVariable = new HashMap<>();
        envVariable.put(JMS_TRADE_REF_NBR_PREFIX, LOCAL_PREFIX + PROPERTY_DEV_PREFIX_DELIMITER + LOCAL2_PREFIX);
        setEnv(envVariable);
        String includeSelector = tradeRefNumberSelector.includeTradePrefix(true);
        Endpoint localSideEndpoint = camelContext.getEndpoint(TOPIC + includeSelector);
        Consumer localSideConsumer = localSideEndpoint.createConsumer(exchange -> {
            log.info("local_host = {}", exchange.getIn().getBody(String.class));
            localMessagesCount.addAndGet(1);
        });
        localSideConsumer.start();

        sendServerAndLocalMessages();
        sendLocalMessages();
        Thread.sleep(10);
        localSideConsumer.stop();

        assertEquals(NUMBER_OF_LOCAL_MESSAGES_MULTIPLE_CHECK, localMessagesCount.get());
    }

    @Test
    public void testServerSideExcludeEmptyVariable() throws Exception {
        AtomicInteger serverMessagesCount = new AtomicInteger(0);
        String excludeSelector = tradeRefNumberSelector.excludeTradePrefix(true);
        Endpoint serverSideEndpoint = camelContext.getEndpoint(TOPIC + excludeSelector);
        Consumer serverSideConsumer = serverSideEndpoint.createConsumer(exchange -> {
            log.info("server_host = {}", exchange.getIn().getBody(String.class));
            serverMessagesCount.addAndGet(1);
        });
        serverSideConsumer.start();

        sendServerAndLocalMessages();
        Thread.sleep(10);
        serverSideConsumer.stop();

        assertEquals(NUMBER_OF_SERVER_MESSAGES + NUMBER_OF_LOCAL_MESSAGES, serverMessagesCount.get());
    }

    @Test
    public void testLocalSideIncludeEmptyVariable() throws Exception {
        AtomicInteger localMessagesCount = new AtomicInteger(0);
        String includeSelector = tradeRefNumberSelector.includeTradePrefix(true);
        Endpoint localSideEndpoint = camelContext.getEndpoint(TOPIC + includeSelector);
        Consumer localSideConsumer = localSideEndpoint.createConsumer(exchange -> {
            log.info("local_host = {}", exchange.getIn().getBody(String.class));
            localMessagesCount.addAndGet(1);
        });
        localSideConsumer.start();

        sendServerAndLocalMessages();
        Thread.sleep(10);
        localSideConsumer.stop();

        assertEquals(NUMBER_OF_SERVER_MESSAGES + NUMBER_OF_LOCAL_MESSAGES, localMessagesCount.get());
    }

    private void sendLocalMessages() {
        for (int i = 0; i < NUMBER_OF_LOCAL_MESSAGES; i++) {
            producerTemplate.sendBodyAndHeader(TOPIC, LOCAL2_PREFIX, MCP_SYSTEM_HEADER, LOCAL2_SYSTEM_HEADER);
        }
    }

    private void sendServerAndLocalMessages() {
        for (int i = 0; i < NUMBER_OF_SERVER_MESSAGES; i++) {
            producerTemplate.sendBodyAndHeader(TOPIC, SERVER_PREFIX, MCP_SYSTEM_HEADER, SERVER_SYSTEM_HEADER);
        }
        for (int i = 0; i < NUMBER_OF_LOCAL_MESSAGES; i++) {
            producerTemplate.sendBodyAndHeader(TOPIC, LOCAL_PREFIX, MCP_SYSTEM_HEADER, LOCAL_SYSTEM_HEADER);
        }
    }
}
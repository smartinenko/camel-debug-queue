package msv.tst.util;

import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ContextManager {

    private ClassPathXmlApplicationContext appContext;

    private SpringCamelContext camelContext;

    public void start() throws Exception {
        this.appContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        this.camelContext = SpringCamelContext.springCamelContext(appContext, false);

        JmsComponent jms = (JmsComponent)appContext.getBean("jms");
        camelContext.addComponent("activemq", jms);
    }

    public void stop() {
        try {
            camelContext.stop();
            camelContext.shutdown();
            appContext.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SpringCamelContext getCamelContext() {
        return camelContext;
    }
}

package jmsdemo;

import java.util.Iterator;
import java.util.List;

import com.solace.services.core.model.SolaceServiceCredentials;
import com.solace.spring.cloud.core.SolaceMessagingInfo;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SpringSolJmsConnectionFactoryCloudFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Service
    static class MessageProducer implements CommandLineRunner {

        private static final Logger logger = LoggerFactory.getLogger(MessageProducer.class);

        @Autowired
        private JmsTemplate jmsTemplate;

        // Other beans that can be used together to create a customized JmsTemplate
        @Autowired private SolConnectionFactory solConnectionFactory;
        @Autowired private SpringSolJmsConnectionFactoryCloudFactory springSolJmsConnectionFactoryCloudFactory;
        @Autowired private SolaceServiceCredentials solaceServiceCredentials;

        /*
        For backwards compatibility:
        - As before, these exist only in the specific scenario where the app is deployed in Cloud Foundry.*/
        @Autowired(required=false) private SolaceMessagingInfo solaceMessagingInfo;

        @Value("${solace.jms.demoQueueName}")
        private String queueName;

        @Override
        public void run(String... strings) throws Exception {
            String msg = "Hello World";
            logger.info("============= Sending " + msg);
            this.jmsTemplate.convertAndSend(queueName, msg);
        }
    }

    @Component
    static class MessageHandler {

        private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

        // Retrieve the name of the queue from the application.properties file
        @JmsListener(destination = "${solace.jms.demoQueueName}")
        public void processMsg(Message msg) {
        	StringBuffer msgAsStr = new StringBuffer("============= Received \nHeaders:");
        	MessageHeaders hdrs = msg.getHeaders();
        	msgAsStr.append("\nUUID: "+hdrs.getId());
        	msgAsStr.append("\nTimestamp: "+hdrs.getTimestamp());
        	Iterator<String> keyIter = hdrs.keySet().iterator();
        	while (keyIter.hasNext()) {
        		String key = keyIter.next();
            	msgAsStr.append("\n"+key+": "+hdrs.get(key));
        	}
        	msgAsStr.append("\nPayload: "+msg.getPayload());
            logger.info(msgAsStr.toString());
        }
    }


}

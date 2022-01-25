package com.azure.spring.sample.servicebus.topic.binder;

import com.azure.spring.integration.core.api.Checkpointer;
// import com.azure.spring.cloud.messaging.core.checkpoint.Checkpointer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

import static com.azure.spring.integration.core.AzureHeaders.CHECKPOINTER;

@SpringBootApplication
public class ServiceBusApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ServiceBusApplication.class, args);
    }

    @Bean
    public Consumer<Message<String>> consume() {
        return message -> {
            Checkpointer checkpointer = (Checkpointer) message.getHeaders().get(CHECKPOINTER);
            LOGGER.info("New message received: '{}'", message.getPayload());
            checkpointer.success().handle((r, ex) -> {
                if (ex == null) {
                    LOGGER.info("Message '{}' successfully checkpointed", message.getPayload());
                } else {
                    LOGGER.error("Some problems are here during checkpoint due to {}", ex.getMessage());
                }
                return null;
            });
        };
    }
}
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.servicebus.topic.binder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Sinks;

@RestController
public class ServiceProducerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceProducerController.class);

    @Autowired
    private Sinks.Many<Message<String>> many;

    ObjectMapper mapper = new ObjectMapper();

    @PostMapping("/messages")
    public ResponseEntity<String> sendMessage(@RequestBody PayloadVO payloadVO) throws Exception {
        String jsonMessage = mapper.writeValueAsString(payloadVO);
        LOGGER.info("Going to add message {} to Sinks.Many.", jsonMessage);
        many.emitNext(MessageBuilder.withPayload(jsonMessage).build(), Sinks.EmitFailureHandler.FAIL_FAST);
        return ResponseEntity.ok("Sent!");
    }
}
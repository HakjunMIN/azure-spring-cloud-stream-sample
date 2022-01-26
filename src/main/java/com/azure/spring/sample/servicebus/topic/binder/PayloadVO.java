package com.azure.spring.sample.servicebus.topic.binder;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayloadVO {
    @JsonProperty
    private String message;
    @JsonProperty
    private String name;
}

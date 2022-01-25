package com.azure.spring.sample.servicebus.topic.binder;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayloadVO {
    private String message;
    private String name;
}

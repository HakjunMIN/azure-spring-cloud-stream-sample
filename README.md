# Spring Cloud Stream을 이용하여 Azure Service Bus Topic사용 샘플


페이로드 VO를 직렬화 하여 메시지를 생산하고 소비할 곳에서 Bean등록만으로 메시지 소비

## Quick Start

1. Azure Service Bus Setup

* Portal을 이용한 Azure Service Bus 생성: https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-topics-subscriptions-portal

> 단, topic을 사용하려면 Basic SKU 이상을 사용해야 함.

1. 커넥션 스트링 설정

```shell
export CONN_STRING="Endpoint=....."
```

3. (선택) 토픽 및 구독 설정

```shell
export TOPIC=topic
export SUBSCRIPTION=sub
```

4. 메시지 생산자용 Spring Boot실행

```shell
mvn spring-boot:run -Dspring-boot.run.profiles=supplier

```

5. 메시지 소비자용 Spring Boot실행

```shell
mvn spring-boot:run -Dspring-boot.run.profiles=consumer

```
> 용이한 로그 확인을 위해 별도의 창에서 실행

6. 메시지 발행

```shell
curl -X POST http://localhost:8080/messages -d '{"message":"hello", "name": "John"}' -H 'content-type: application/json'
```

7. 메시지 생산 로그 확인 확인 
`-Dspring-boot.run.profiles=supplier` 로 구동한 Spring Boot의 로그에 아래와 같이 나오는지 확인

```
...
Manually sending message GenericMessage [payload={"message":"hello","name":"John"}, headers={id=52c5833c-7a4b-a176-f3df-b40cda178e83, timestamp=1643182645367}]
```

8. 메시지 소비 로그 확인
`-Dspring-boot.run.profiles=consumer` 로 구동한 Spring Boot의 로그에 아래와 같이 나오는지 확인

```
...
New message received: '{"message":"hello","name":"Joh69eeee99"}'
```

### 메시지 생산 부분

#### 메시지 페이로드

```Java
public class PayloadVO {
    @JsonProperty
    private String message;
    @JsonProperty
    private String name;
}
```

#### 메시지 생산 설정 Bean

메시지를 생성해 낼 Bean등록. Flux구조를 이용하여 이벤트 발생시, 오류 발생시 로직을 별도로 구현.

```Java
@Bean
public Sinks.Many<Message<String>> many() {
    return Sinks.many().multicast().onBackpressureBuffer();
}

@Bean
public Supplier<Flux<Message<String>>> supply(Sinks.Many<Message<String>> many) {
    return () -> many.asFlux()
                        .doOnNext(m -> LOGGER.info("Manually sending message {}", m))
                        .doOnError(t -> LOGGER.error("Error encountered", t));
```

#### 메시지 생산

VO를 `json`으로 직렬화하여 메시지 생산

```Java
    ...
    @Autowired
    private Sinks.Many<Message<String>> many;
    ...
    String jsonMessage = mapper.writeValueAsString(payloadVO);
    many.emitNext(MessageBuilder.withPayload(jsonMessage).build(), Sinks.EmitFailureHandler.FAIL_FAST);
    ...
```

### 메시지 소비

아래 Consumer Bean의 consume()를 오버라이드 하여 사용.

```Java
@Bean
public Consumer<Message<String>> consume() { }
```

#### 서비스 단절을 대비한 Retry설정

메시지 생산 시 Azure Service Bus와의 단절을 대비하기 위한 Retry 설정 권고

```yaml
spring:
  cloud:
    azure:
      retry:
        timeout: 5
        max-attempts: 3
        backoff:
          delay: 10
          multiplier: 2
```
> 메시지 생산, 소비 부분에 각각 넣을 수 있으며 메시지의 갯수, 1건당 용량, Java Heap 사이즈 등을 고려하여 `max-attempts`, `backoff.delay`등을 설정

다른설정은 하기 링크 참고
[Link](https://microsoft.github.io/spring-cloud-azure/current/reference/html/index.html#spring-cloud-stream-binder-for-azure-service-bus)

### 참고

* Microsoft 공식문서: https://docs.microsoft.com/ko-kr/azure/developer/java/spring-framework/configure-spring-cloud-stream-binder-java-app-with-service-bus

* Spring Cloud Stream binder for Azure Servic Bus: https://github.com/microsoft/spring-cloud-azure/blob/4.0.0-beta.3/docs/src/main/asciidoc/spring-cloud-stream-support.adoc#spring-cloud-stream-binder-for-azure-service-bus
 
* 샘플코드: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/servicebus/azure-spring-cloud-stream-binder-servicebus-topic/servicebus-topic-binder

# Spring Cloud Stream을 이용하여 Azure Service Bus Topic사용 샘플

페이로드 VO를 직렬화 하여 메시지를 생산하고 소비할 곳에서 Bean등록만으로 메시지 소비

## Quick Start

1. Azure Service Bus Setup
<https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-topics-subscriptions-portal>

> 단, topic을 사용하려면 Basic SKU 이상을 사용해야 함.

2. 커넥션 스트링 설정

```shell
export CONN_STRING="Endpoint=....."
```

3. (선택) 토픽 및 구독 설정

```shell
export TOPIC=topic
export SUBSCRIPTION=sub
```

4. 스프링 boot실행

```shell
mvn spring-boot:run
```

5. 메시지 발행

```shell
curl -X POST http://localhost:8080/messages -d '{"message":"hello", "name": "John"}' -H 'content-type: application/json'
```

6. 메시지 수신 확인
로그와 같이 메시지 수신 항목이 나오는지 확인

```
 ... New message received: '{"message":"hello","name":"John"}'
```

### 메시지 생산 부분

#### 메시지 페이로드

```Java
public class PayloadVO {
    private String message;
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

#### Retry Configuration

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

다른설정은 하기 링크 참고
[Link](https://microsoft.github.io/spring-cloud-azure/current/reference/html/index.html#spring-cloud-stream-binder-for-azure-service-bus)

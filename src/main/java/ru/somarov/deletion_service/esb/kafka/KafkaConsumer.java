package ru.somarov.deletion_service.esb.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static ru.somarov.deletion_service.constant.Constants.IN_ROUTE;

/**
 * This class is a kafka listener holder.
 * It is enabled by `app.kafka.consuming-enabled` property
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Service
@Slf4j
@ConditionalOnProperty(value = "app.kafka.consuming-enabled", havingValue = "true")
@RequiredArgsConstructor
public class KafkaConsumer {

    /**
     * Camel producer
     */
    private final ProducerTemplate producerTemplate;


    /**
     * Function gets business info and headers from incoming kafka message, fills camel exchange
     * with it and sends exchange further in processing pipeline (to incoming camel route)
     *
     * @param body Json body of kafka message
     * @param headers Map of header objects
     * @since 1.0.0
     *
    */
    @KafkaListener(id = "json-consumer", clientIdPrefix = "json",
                   topics = "#{'${app.kafka.consumer.json.topics}'.split(',')}",
                   groupId = "${spring.kafka.consumer.group-id}")
    public void consumeJsonEvent(@Payload String body, @Headers Map<String, Object> headers) {
        var exchangeBuilder = ExchangeBuilder.anExchange(producerTemplate.getCamelContext()).withBody(body);
        Optional.ofNullable(headers).ifPresent(it -> it.forEach(exchangeBuilder::withHeader));
        producerTemplate.send(IN_ROUTE, exchangeBuilder.build());
    }
}

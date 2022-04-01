package ru.somarov.deletion_service.esb.routes.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import ru.somarov.deletion_service.esb.dto.EsbMessage;

import java.util.Map;
import java.util.Optional;

/**
 * This class is a helper which is responsible for forming final look of esb message,
 * which will be sent via kafka. It is used in outcoming kafka camel route
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageFormer implements Processor {

    private final ObjectMapper mapper;

    /**
     * Function gets filled exchange and transforms business info in kafka-like esb message
     *
     * @param exchange Outcoming camel exchange filled with business info
     * @since 1.0.0
     *
    */
    @Override
    public void process(Exchange exchange) throws JsonProcessingException {
        var esbMessage = exchange.getIn().getBody(EsbMessage.class);
        Map<String, Object> headers = exchange.getIn().getHeaders();
        String payload  = mapper.writeValueAsString(esbMessage);
        String customTopic = (String) headers.get("TOPIC");
        Object clientId = Optional.ofNullable(headers.get("CLIENT_ID")).map(String::valueOf).orElse(null);
        log.debug("Got esb message to form into kafka-like: payload - {}, topic - {}, clientId - {}", payload, customTopic, clientId);
        Message<String> message = MessageBuilder
                .withPayload(payload)
                .copyHeaders(headers)
                .setHeader(KafkaHeaders.TOPIC, customTopic)
                .setHeader(KafkaHeaders.MESSAGE_KEY, clientId)
                .build();
        exchange.getIn().setBody(message, Message.class);
    }
}

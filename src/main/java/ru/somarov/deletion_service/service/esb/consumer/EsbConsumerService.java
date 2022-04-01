package ru.somarov.deletion_service.service.esb.consumer;

import ru.somarov.deletion_service.service.esb.consumer.handler.IEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is a proxy between esb transport layer and business service layer.
 * All message processing should be moved out to separate implementation of {@link IEventHandler}
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Slf4j
@Service
public class EsbConsumerService {
    private final Map<String, IEventHandler> handlers;

    public EsbConsumerService(List<IEventHandler> handlers) {
        this.handlers = handlers.stream().collect(Collectors.toMap(IEventHandler::type, Function.identity()));
    }

    /**
     * Used in incoming camel route
     * @param exchange Camel exchange with parsed incoming message info
     */
    public void handle(Exchange exchange) {
        Map<String, Object> headers = exchange.getIn().getHeaders();

        String type = (String) headers.get("MESSAGE_TYPE");
        IEventHandler handler = handlers.get(type);
        if (handler == null) {
            log.warn("Handler for message with entity type " + type + " isn't registered. Skipping message " + exchange.getIn());
            return;
        }
        log.debug("Handler " + handler.getClass().getSimpleName() + " has been chosen for message " + exchange.getIn().getBody());
        handler.handle(exchange);
    }
}

package ru.somarov.deletion_service.service.esb.consumer.handler.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;
import ru.somarov.deletion_service.esb.dto.Result;
import ru.somarov.deletion_service.service.completion.CompletionService;
import ru.somarov.deletion_service.service.esb.consumer.handler.IEventHandler;

import static ru.somarov.deletion_service.constant.Constants.ESB_MESSAGE_TYPE_RESULT;

/**
 * Handler for processing results from other systems
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResultEventHandler implements IEventHandler {

    private final CompletionService service;

    @Override
    public void handle(Exchange exchange) {
        Result event = exchange.getIn().getBody(Result.class);
        log.debug("Starting to handle result message: " + event);

        if(event == null) {
            log.error("Event is null. Skipping processing");
            return;
        }

        String producer = (String) exchange.getIn().getHeaders().get("MESSAGE_PRODUCER");

        service.completeAction(Long.parseLong(event.getClientId()), event.getStatus(), producer);

        log.debug("Result message has been handled: {}", event);
    }

    @Override
    public String type() {
        return ESB_MESSAGE_TYPE_RESULT;
    }
}

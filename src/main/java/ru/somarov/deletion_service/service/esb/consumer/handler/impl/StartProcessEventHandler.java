package ru.somarov.deletion_service.service.esb.consumer.handler.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;
import ru.somarov.deletion_service.constant.state_machine.SmEvent;
import ru.somarov.deletion_service.esb.dto.StartProcess;
import ru.somarov.deletion_service.service.esb.consumer.handler.IEventHandler;
import ru.somarov.deletion_service.service.state_machine.StateMachineService;

import static ru.somarov.deletion_service.constant.Constants.ESB_MESSAGE_TYPE_START_PROCESS;

/**
 * Handler for processing start process esb message
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartProcessEventHandler implements IEventHandler {

    private final StateMachineService service;

    @Override
    public void handle(Exchange exchange) {
        StartProcess event = exchange.getIn().getBody(StartProcess.class);
        log.debug("Starting to handle start process message: " + event);

        if(event == null) {
            log.error("Event is null. Skipping processing");
            return;
        }

        service.start(Long.parseLong(event.getClientId()), SmEvent.WEB_REQUEST_STARTED, event.getGroup());

        log.debug("Start process message has been handled: {}", event);
    }

    @Override
    public String type() {
        return ESB_MESSAGE_TYPE_START_PROCESS;
    }
}

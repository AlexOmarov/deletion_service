package ru.somarov.deletion_service.util;

import ru.somarov.deletion_service.service.esb.consumer.handler.IEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;

@Slf4j
public class DummyEventHandler implements IEventHandler {
    @Override
    public void handle(Exchange exchange) {
        log.info("Handling message " + exchange.getIn());
    }

    @Override
    public String type() {
        return "foo";
    }
}

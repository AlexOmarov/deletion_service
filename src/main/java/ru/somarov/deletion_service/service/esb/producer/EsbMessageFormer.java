package ru.somarov.deletion_service.service.esb.producer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;
import ru.somarov.deletion_service.domain.entity.Client;
import ru.somarov.deletion_service.esb.dto.Command;
import ru.somarov.deletion_service.esb.dto.EsbMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a helper which is responsible for filling outcoming esb messages with
 * business info depending on passed values and message type
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Slf4j
@Component
@AllArgsConstructor
public class EsbMessageFormer {

    /**
     * Function fills passed camel exchange with esb message
     *
     * @param exchange  Camel exchange that should be filled with business info
     *                  (will be processed later, when it will come into outcome camel route)
     * @param client    Client info
     * @param recipient Recipient of message
     * @param topic     Topic in which message should be sent
     * @since 1.0.0
     *
     */
    public void fillExchangeWithCommand(Exchange exchange, Client client, String recipient, String topic) {
        var command = new Command();
        command.setClientId(String.valueOf(client.getId()));
        command.setGroup(client.getGroup().getCode());

        Map<String, Object> headers = new HashMap<>();
        headers.put("MESSAGE_RECIPIENT", recipient);
        headers.put("TOPIC", topic);
        headers.put("MESSAGE_PRODUCER", topic);

        exchange.getIn().setBody(command, EsbMessage.class);
        exchange.getIn().setHeaders(headers);
    }
}

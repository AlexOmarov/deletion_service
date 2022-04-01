package ru.somarov.deletion_service.service.esb.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.somarov.deletion_service.constant.SideSystem;
import ru.somarov.deletion_service.domain.entity.Client;

import java.util.List;

import static ru.somarov.deletion_service.constant.Constants.OUT_ROUTE;

/**
 * This class is a producer of available outcoming esb messages
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EsbProducerService {

    private final ProducerTemplate producerTemplate;
    private final EsbMessageFormer converter;

    public void send(Client client, List<Pair<SideSystem, String>> recipients) {
        log.debug("Sending command for client: {}", client);
        recipients.forEach(recipient -> {
            var exchange = ExchangeBuilder.anExchange(producerTemplate.getCamelContext()).build();
            converter.fillExchangeWithCommand(exchange, client, recipient.getLeft().name(), recipient.getRight());
            producerTemplate.send(OUT_ROUTE, exchange);
        });
        log.debug("Sending command is over for client {}", client);
    }

}

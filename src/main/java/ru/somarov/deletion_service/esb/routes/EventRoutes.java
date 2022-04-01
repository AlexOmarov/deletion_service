package ru.somarov.deletion_service.esb.routes;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import ru.somarov.deletion_service.esb.routes.kafka.KafkaMessageFormer;
import ru.somarov.deletion_service.service.esb.consumer.EsbConsumerService;

import static ru.somarov.deletion_service.constant.Constants.IN_ROUTE;
import static ru.somarov.deletion_service.constant.Constants.OUT_ROUTE;

/**
 * This class is a configuration of camel esb routes
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Component
@RequiredArgsConstructor
public class EventRoutes extends RouteBuilder {

    /**
     * Former of kafka-specific messages. Used only if we use kafka as message broker
     */
    private final KafkaMessageFormer former;
    /**
     * Service which handles every esb message from any source
     */
    private final EsbConsumerService esbConsumerService;

    @Override
    public void configure() {
        /* Route for all incoming esb messages from any source */
        from(IN_ROUTE).autoStartup("{{app.kafka.consuming-enabled}}").id("event-consumption-route")
                .choice()
                    .when(simple("${header.MESSAGE_TYPE} in '{{app.kafka.consuming.events-filter}}'"))
                        .log("Incoming message from Kafka \n\r-------------\n\rHeaders: ${headers}\n\rPayload: ${body}\n\r--------------")
                        .bean(esbConsumerService, "handle").id("event-route-in")
                    .otherwise()
                        .log("Skipped message: ${headers}").id("log-error")
                .endChoice();

        /* Route for all incoming esb messages from any source (for now using kafka as message broker)*/
        from(OUT_ROUTE).autoStartup("{{app.kafka.producing-enabled}}").id("push-event-kafka-route")
                .process(former)
                .log("Outcoming message to Kafka \n\r-------\n\rHeaders: ${headers}\n\rPayload: ${body}\n\r-------")
                .to("bean:kafkaTemplate?method=send").id("kafka-route-out");
    }
}

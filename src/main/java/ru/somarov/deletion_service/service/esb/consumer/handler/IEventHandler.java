package ru.somarov.deletion_service.service.esb.consumer.handler;

import org.apache.camel.Exchange;

/**
 * Interface for defining contract of esb message handler
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
public interface IEventHandler {

    /**
     * Method accepts camel exchange filled with message's info and performs required business logic
     *
     * @param exchange Canmel exchange with incoming message's info
     * @since
     * 2022-01-31
     */
    void handle(Exchange exchange);

    /**
     * Function returns type of esb message, that should be handled with this handler implementation
     *
     * @return String esb message type
     * @since
     * 2022-01-31
    */
    String type();
}

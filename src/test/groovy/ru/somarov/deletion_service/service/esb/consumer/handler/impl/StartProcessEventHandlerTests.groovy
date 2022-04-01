package ru.somarov.deletion_service.service.esb.consumer.handler.impl

import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.ExchangeBuilder
import org.mockito.InjectMocks
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import ru.somarov.deletion_service.Application
import ru.somarov.deletion_service.conf.TestDataSourceConfiguration
import ru.somarov.deletion_service.constant.state_machine.SmEvent
import ru.somarov.deletion_service.esb.dto.StartProcess
import ru.somarov.deletion_service.service.state_machine.StateMachineService
import spock.lang.Specification

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*
import static ru.somarov.deletion_service.constant.Constants.ESB_MESSAGE_TYPE_START_PROCESS

@Import(TestDataSourceConfiguration)
@ActiveProfiles(profiles = ["test"])
@ContextConfiguration(classes = [Application])
@SpringBootTest
class StartProcessEventHandlerTests extends Specification {

    @Autowired
    @InjectMocks
    StartProcessEventHandler handler

    @Autowired
    ProducerTemplate producerTemplate

    @MockBean
    StateMachineService service

    def setup() {
        reset(service)
    }

    def "When handle method is called with valid succeeded message, then message will be processed"() {
        setup:
        StartProcess entity = new StartProcess()
        entity.setClientId("1")
        entity.setGroup("DEFAULT")
        var exchange = ExchangeBuilder.anExchange(producerTemplate.getCamelContext()).withBody(entity).build()
        when:
        handler.handle(exchange)
        then:
        verify(service, times(1)).start(anyLong(), nullable(SmEvent.class), anyString()) == null
    }

    def "When get type method is called then esb message type is returned"() {
        when:
        def type = handler.type()
        then:
        type == ESB_MESSAGE_TYPE_START_PROCESS
    }
}

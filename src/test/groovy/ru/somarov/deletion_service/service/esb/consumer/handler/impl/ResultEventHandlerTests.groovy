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
import ru.somarov.deletion_service.esb.dto.Result
import ru.somarov.deletion_service.service.completion.CompletionService
import spock.lang.Specification

import static org.mockito.ArgumentMatchers.anyLong
import static org.mockito.ArgumentMatchers.nullable
import static org.mockito.Mockito.*
import static ru.somarov.deletion_service.constant.Constants.ESB_MESSAGE_TYPE_RESULT

@Import(TestDataSourceConfiguration)
@ActiveProfiles(profiles = ["test"])
@ContextConfiguration(classes = [Application])
@SpringBootTest
class ResultEventHandlerTests extends Specification {

    @Autowired
    @InjectMocks
    ResultEventHandler handler

    @Autowired
    ProducerTemplate producerTemplate

    @MockBean
    CompletionService service

    def setup() {
        reset(service)
    }

    def "When handle method is called with valid succeeded message, then message will be processed"() {
        given:
        Result result = new Result()
        result.setClientId("1")
        result.setGroup("DEFAULT")
        var exchange = ExchangeBuilder.anExchange(producerTemplate.getCamelContext()).withBody(result).build()
        when:
        handler.handle(exchange)
        then:
        verify(service, times(1)).completeAction(anyLong(),nullable(String.class),nullable(String.class))
    }

    def "When handle method is called with valid succeeded message and CLTA producer, then message will be processed as clearing"() {
        given:
        Result result = new Result()
        result.setClientId("1")
        result.setGroup("DEFAULT")
        var exchange = ExchangeBuilder.anExchange(producerTemplate.getCamelContext()).withBody(result).build()
        when:
        handler.handle(exchange)
        then:
        verify(service, times(1)).completeAction(anyLong(),nullable(String.class),nullable(String.class))
    }

    def "When handle method is called with invalid info, then message won't be processed"() {
        given:
        var exchange = ExchangeBuilder.anExchange(producerTemplate.getCamelContext()).withBody(null).build()
        when:
        handler.handle(exchange)
        then:
        verify(service, times(0)).completeAction(anyLong(),nullable(String.class),nullable(String.class))

    }

    def "When get type method is called then esb message type is returned"() {
        when:
        def type = handler.type()
        then:
        type == ESB_MESSAGE_TYPE_RESULT
    }
}

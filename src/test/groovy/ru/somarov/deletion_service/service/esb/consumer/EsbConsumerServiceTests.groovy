package ru.somarov.deletion_service.service.esb.consumer

import ru.somarov.deletion_service.Application
import ru.somarov.deletion_service.conf.TestDataSourceConfiguration
import ru.somarov.deletion_service.util.DummyEventHandler
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.ExchangeBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Unroll

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

@Import(TestDataSourceConfiguration)
@ActiveProfiles(profiles = ["test"])
@ContextConfiguration(classes = [Application])
@SpringBootTest
class EsbConsumerServiceTests extends Specification {

    @SpyBean
    DummyEventHandler dummyEventHandler

    @Autowired
    ProducerTemplate producerTemplate

    @Autowired
    EsbConsumerService esbService

    @Unroll
    def "When event comes to esbService it finds suitable handler and calls it"() {
        given:
        ExchangeBuilder exchangeBuilder = ExchangeBuilder.anExchange(producerTemplate.getCamelContext())
        ['MESSAGE_CREATED': '2020-04-23T10:05:26.000',
         'MESSAGE_TYPE'   : 'ENTITY_EVENT',
         'ENTITY_STATUS'  : 'succeeded',
         'MESSAGE_ID'     : '111',
         'ENTITY_TYPE'    : type].each { exchangeBuilder.withHeader(it.key, it.value) }
        Exchange exchange = exchangeBuilder.build()
        when:
        esbService.handle(exchange)
        then:
        verify(dummyEventHandler, times(times)).handle(any()) == null
        where:
        type     | times
        "bar"    | 0
        "foobar" | 0
    }
}

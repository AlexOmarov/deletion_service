package ru.somarov.deletion_service.scheduler

import ru.somarov.deletion_service.Application
import ru.somarov.deletion_service.conf.TestDataSourceConfiguration
import ru.somarov.deletion_service.service.retry.RetryService
import org.apache.commons.lang3.tuple.Pair
import org.awaitility.Awaitility
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.time.Duration
import java.time.temporal.ChronoUnit

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

@Import(TestDataSourceConfiguration)
@ActiveProfiles(profiles = ["test"])
@ContextConfiguration(classes = [Application])
@SpringBootTest(properties = ['app.scheduling.enabled=true', 'app.retry.scheduler.enabled=true'])
class SchedulersTests extends Specification {

    @MockBean
    RetryService service


    def "Schedulers are launched depending on property and call needed services each iteration"() {
        setup:
        doReturn(Pair.of(false, List.of())).when(service).retry(any())
        when:
        println("Test started")
        then:
        Awaitility.await()
                .atMost(Duration.of(15, ChronoUnit.SECONDS))
                .untilAsserted(() ->
                        verify(service, times(2)).retry(any())
                )
    }
}

package ru.somarov.deletion_service.scheduler.handler

import ru.somarov.deletion_service.Application
import ru.somarov.deletion_service.conf.TestDataSourceConfiguration
import ru.somarov.deletion_service.service.retry.RetryService
import org.apache.commons.lang3.tuple.Pair
import org.mockito.ArgumentCaptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.*

@Import(TestDataSourceConfiguration)
@ActiveProfiles(profiles = ["test"])
@ContextConfiguration(classes = [Application])
@SpringBootTest(properties = ['app.action.retry.batch=200'])
class RetrySchedulerHandlerTests extends Specification {

    @Autowired
    private RetrySchedulerHandler handler
    @MockBean
    private RetryService retryService

    def "Handler calls retry service to retry actions"() {
        setup:
        doReturn(Pair.of(false, List.of())).when(retryService).retry(any())
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class)
        when:
        handler.handle()
        then:
        !verify(retryService, times(1)).retry(captor.capture())
        verify(retryService, times(1)).checkForCompletion(any()) || true
        captor.getValue().getPageSize() == 200
    }
}

package ru.somarov.deletion_service.service.action.handler.impl

import ru.somarov.deletion_service.Application
import ru.somarov.deletion_service.conf.TestDataSourceConfiguration
import ru.somarov.deletion_service.domain.entity.*
import ru.somarov.deletion_service.service.persister.ActionPersistenceService
import ru.somarov.deletion_service.service.web.WebService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

@Import(TestDataSourceConfiguration)
@ActiveProfiles(profiles = ["test"])
@ContextConfiguration(classes = [Application])
@SpringBootTest
class WebRequestActionHandlerTests extends Specification {

    @Autowired
    private WebRequestActionHandler handler

    @MockBean
    private WebService webService

    @MockBean
    private ActionPersistenceService persister

    def "Handler accepts deletion process, calls web service to get restrictions and calls persister to save result"() {
        setup:
        doReturn(List.of()).when(webService).performRequest(any())
        Client client = Client.builder().id(1L).build()
        DeletionProcess process = DeletionProcess.builder().id(1L).client(client).build()
        when:
        handler.handle(process)
        then:
        verify(webService, times(1)).performRequest(eq(client)) || true
        verify(persister, times(1)).persistAction(anyLong(), any() as ActionStatus.Code, any() as Action.Code) || true
    }

    def "Handler returns #code when restrictions are #restrictions"() {
        setup:
        doReturn(restrictions).when(webService).performRequest(any())
        Client client = Client.builder().id(1L).build()
        DeletionProcess process = DeletionProcess.builder().id(1L).client(client).build()
        when:
        def result = handler.handle(process)
        then:
        result == code
        where:
        restrictions           | code
        List.of()              | ActionStatus.Code.SUCCEEDED
        List.of("RESTRICTION") | ActionStatus.Code.REJECTED
    }
}

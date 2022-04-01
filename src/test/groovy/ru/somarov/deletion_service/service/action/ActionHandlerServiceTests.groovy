package ru.somarov.deletion_service.service.action

import ru.somarov.deletion_service.Application
import ru.somarov.deletion_service.conf.TestDataSourceConfiguration
import ru.somarov.deletion_service.domain.entity.Action
import ru.somarov.deletion_service.domain.entity.ActionStatus
import ru.somarov.deletion_service.domain.entity.DeletionProcess
import ru.somarov.deletion_service.service.action.handler.impl.FirstActionHandler
import ru.somarov.deletion_service.service.action.handler.impl.WebRequestActionHandler
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static org.mockito.Mockito.*

@Import(TestDataSourceConfiguration)
@ActiveProfiles(profiles = ["test"])
@ContextConfiguration(classes = [Application])
@SpringBootTest
class ActionHandlerServiceTests extends Specification {

    @Autowired
    ActionHandlerService service

    @SpyBean
    private WebRequestActionHandler checkHandler
    @MockBean
    private FirstActionHandler firstActionHandler


    def "changeState method doesn't call handler if handler cannot be found and returns error"() {
        when:
        def code = service.handle(DeletionProcess.builder().build(), Action.Code.FIRST_ACTION)
        then:
        code == ActionStatus.Code.FAILED
        verify(firstActionHandler, times(0)).handle(Mockito.any()) == null
    }
}

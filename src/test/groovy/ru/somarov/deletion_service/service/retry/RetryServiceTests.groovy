package ru.somarov.deletion_service.service.retry

import ru.somarov.deletion_service.Application
import ru.somarov.deletion_service.conf.TestDataSourceConfiguration
import ru.somarov.deletion_service.domain.entity.Action
import ru.somarov.deletion_service.domain.entity.ActionStatus
import ru.somarov.deletion_service.domain.entity.DeletionProcess
import ru.somarov.deletion_service.domain.entity.ProcessStageAction
import ru.somarov.deletion_service.domain.repository.Dao
import ru.somarov.deletion_service.service.action.ActionHandlerService
import ru.somarov.deletion_service.service.completion.CompletionService
import org.apache.camel.test.spring.UseAdviceWith
import org.mockito.ArgumentCaptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.time.LocalDateTime

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

@Import(TestDataSourceConfiguration)
@ActiveProfiles(profiles = ["test"])
@ContextConfiguration(classes = [Application])
@UseAdviceWith
@SpringBootTest
class RetryServiceTests extends Specification {

    @Autowired
    private RetryService service

    @MockBean
    private Dao dao
    @MockBean
    private ActionHandlerService actionHandlerService
    @MockBean
    private CompletionService smService

    def "retry method gets actions for retry with passed limit, calls handler for each action and saves actions with new statuses"() {
        setup:

        long failedActionId = 1L
        PageRequest request = PageRequest.of(0, 200)
        Page<ProcessStageAction> actions = new PageImpl(List.of(
                ProcessStageAction.builder()
                        .id(failedActionId)
                        .action(Action.builder().code(Action.Code.FIRST_ACTION.name()).build())
                        .actionStatus(ActionStatus.builder().code(ActionStatus.Code.FAILED.name()).build())
                        .updated(LocalDateTime.now())
                        .errorCount(0)
                        .build(),
                ProcessStageAction.builder()
                        .id(failedActionId)
                        .updated(LocalDateTime.now())
                        .errorCount(0)
                        .action(Action.builder().code(Action.Code.FIRST_ACTION.name()).build())
                        .actionStatus(ActionStatus.builder().code(ActionStatus.Code.FAILED.name()).build())
                        .build()
        ))

        ArgumentCaptor<List<ProcessStageAction>> captor = ArgumentCaptor.forClass(List<ProcessStageAction>.class)

        doReturn(actions).when(dao).getActionsForRetry(any())
        doReturn(ActionStatus.builder().code(ActionStatus.Code.IN_PROGRESS.name()).build()).when(dao).getActionStatus(anyString())
        doReturn(ActionStatus.Code.IN_PROGRESS).when(actionHandlerService).handle(any(), any())
        when:
        service.retry(request)
        then:
        verify(dao, times(1)).getActionsForRetry(eq(request)) || true
        actions.getContent().forEach(action -> {
            verify(actionHandlerService, times(1)).handle(any(), eq(Action.Code.valueOf(((ProcessStageAction) action).getAction().getCode()))) || true
        })
        verify(dao, times(1)).saveActions(captor.capture()) || true
        def list = captor.getValue()
        list.stream().filter(action -> action.getId() == failedActionId
                && action.getActionStatus().getCode() == ActionStatus.Code.IN_PROGRESS.name()).findFirst().isPresent()

    }

    def "checkForCompletion method calls sm service for checking completion of processes"() {
        setup:
        List<DeletionProcess> processes = List.of(DeletionProcess.builder().build(), DeletionProcess.builder().build())
        when:
        service.checkForCompletion(processes)
        then:
        verify(smService, times(2)).checkAndSendNextEvent(any())
    }
}

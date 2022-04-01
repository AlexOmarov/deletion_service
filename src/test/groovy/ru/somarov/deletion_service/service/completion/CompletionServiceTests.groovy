package ru.somarov.deletion_service.service.completion

import ru.somarov.deletion_service.Application
import ru.somarov.deletion_service.conf.TestDataSourceConfiguration
import ru.somarov.deletion_service.constant.SideSystem
import ru.somarov.deletion_service.constant.state_machine.SmEvent
import ru.somarov.deletion_service.domain.entity.*
import ru.somarov.deletion_service.domain.repository.Dao
import ru.somarov.deletion_service.service.persister.ActionPersistenceService
import ru.somarov.deletion_service.service.state_machine.StateMachineService
import org.apache.camel.test.spring.UseAdviceWith
import org.mockito.ArgumentCaptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
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
class CompletionServiceTests extends Specification {
    @Autowired
    private CompletionService service
    
    @MockBean
    private StateMachineService smService
    @MockBean
    private ActionPersistenceService persister
    @MockBean
    private Dao dao


    def "completeAction calls persister to save action based on passed parameters"() {
        setup:
        long id = 1L
        String status = "SUCCEEDED"
        String producer = "FIRST_SYSTEM"
        ActionStatus.Code result = ActionStatus.Code.SUCCEEDED
        SideSystem system = SideSystem.FIRST_SYSTEM
        List<ProcessStageAction> actions = List.of(
                ProcessStageAction.builder().actionStatus(
                        ActionStatus.builder().code(ActionStatus.Code.IN_PROGRESS.name()).build()
                ).build()
        )


        def process = DeletionProcess.builder()
                .id(1L)
                .client(Client.builder().id(1L).build())
                .stage(Stage.builder().code(Stage.Code.ASYNC_STAGE.name()).build())
                .updated(LocalDateTime.now())
                .build()

        ProcessStageAction psa = ProcessStageAction.builder().process(process).actionStatus(ActionStatus.builder()
                .code(ActionStatus.Code.IN_PROGRESS.name())
                .build()).build()

        doReturn(actions).when(dao).getActions(anyLong())
        doReturn(psa).when(persister).persistAction(anyLong(), any() as ActionStatus.Code, any() as SideSystem)
        when:
        service.completeAction(id, status, producer)
        then:
        verify(persister, times(1)).persistAction(eq(id), eq(result) as ActionStatus.Code, eq(system) as SideSystem) || true
        verify(smService, times(0)).sendNextEvent(any(), any()) || true
    }

    def "checkAndSendNextEvent doesn't send event when process is uncompleted"() {
        setup:
        List<ProcessStageAction> actions = List.of(
                ProcessStageAction.builder().actionStatus(
                        ActionStatus.builder().code(ActionStatus.Code.IN_PROGRESS.name()).build()
                ).build()
        )

        def process = DeletionProcess.builder()
                .id(1L)
                .client(Client.builder().id(1L).build())
                .stage(Stage.builder().code(Stage.Code.ASYNC_STAGE.name()).build())
                .updated(LocalDateTime.now())
                .build()

        doReturn(actions).when(dao).getActions(anyLong())
        when:
        service.checkAndSendNextEvent(process)
        then:
        verify(smService, times(0)).sendNextEvent(any(), any()) || true
    }
}

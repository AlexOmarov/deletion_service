package ru.somarov.deletion_service.service.persister

import org.mockito.ArgumentCaptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import ru.somarov.deletion_service.Application
import ru.somarov.deletion_service.conf.TestDataSourceConfiguration
import ru.somarov.deletion_service.domain.entity.*
import ru.somarov.deletion_service.domain.repository.Dao
import ru.somarov.deletion_service.service.persister.TransitionPersistenceService
import spock.lang.Specification

import java.time.LocalDateTime

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

@Import(TestDataSourceConfiguration)
@ActiveProfiles(profiles = ["test"])
@ContextConfiguration(classes = [Application])
@SpringBootTest
class TransitionPersistenceServiceTests extends Specification {

    @Autowired
    private TransitionPersistenceService persister

    @MockBean
    private Dao dao

    def "persistTransition saves process with new stage, removes all previous actions and adds new depending on passed args"() {
        setup:
        def timeOfOperationStart = LocalDateTime.now()
        def process = DeletionProcess.builder()
                .id(1L)
                .stage(Stage.builder().code(Stage.Code.ASYNC_STAGE.name()).build())
                .build()
        def stage = Stage.Code.ASYNC_STAGE
        def actionCodes = List.of(Action.Code.FIRST_ACTION)
        ArgumentCaptor<List<ProcessStageAction>> captor = ArgumentCaptor.forClass(List<ProcessStageAction>.class)
        doReturn(Stage.builder().code(Stage.Code.ASYNC_STAGE.name()).build()).when(dao).getStage(eq(Stage.Code.ASYNC_STAGE.name()))
        doReturn(process).when(dao).getDeletionProcess(anyLong())
        doReturn(Action.builder().code(Action.Code.FIRST_ACTION.name()).build()).when(dao).getAction(eq(Action.Code.FIRST_ACTION.name()))
        doReturn(ActionStatus.builder().code(ActionStatus.Code.IN_PROGRESS.name()).build()).when(dao).getActionStatus(eq(ActionStatus.Code.IN_PROGRESS.name()))
        when:
        def result = persister.persistTransition(process.getId(), stage, actionCodes)
        then:
        verify(dao, times(1)).removeAllActions(eq(process))
        verify(dao, times(1)).saveDeletionProcess(eq(process)) == null
        verify(dao, times(1)).saveActions(captor.capture()) == null
        result
        def actions = captor.getValue()
        actions.stream().filter(action -> action.getAction().getCode() == Action.Code.FIRST_ACTION.name()
                && action.getActionStatus().getCode() == ActionStatus.Code.IN_PROGRESS.name()
                && action.getErrorCount() == 0).findFirst().orElse(null) != null
        process.getStage().getCode() == Stage.Code.ASYNC_STAGE.name()
        process.getUpdated() >= timeOfOperationStart
    }

    def "persistTransition doesn't save process if process is already in same stage"() {
        setup:
        def process = DeletionProcess.builder()
                .id(1L)
                .stage(Stage.builder().code(Stage.Code.ASYNC_STAGE.name()).build())
                .build()
        def stage = Stage.Code.ASYNC_STAGE
        def actionCodes = List.of(Action.Code.FIRST_ACTION)
        doReturn(Stage.builder().code(Stage.Code.ASYNC_STAGE.name()).build()).when(dao).getStage(eq(Stage.Code.ASYNC_STAGE.name()))
        doReturn(Action.builder().code(Action.Code.FIRST_ACTION.name()).build()).when(dao).getAction(eq(Action.Code.FIRST_ACTION.name()))
        doReturn(ActionStatus.builder().code(ActionStatus.Code.IN_PROGRESS.name()).build()).when(dao).getActionStatus(eq(ActionStatus.Code.IN_PROGRESS.name()))
        doReturn(process).when(dao).getDeletionProcess(anyLong())
        when:
        def result = persister.persistTransition(process.getId(), stage, actionCodes)
        then:
        verify(dao, times(0)).removeAllActions(eq(process))
        verify(dao, times(0)).saveDeletionProcess(eq(process)) == null
        verify(dao, times(0)).saveActions(any()) == null
        !result
    }

    def "persistTransition doesn't add new actions if the are not passed"() {
        setup:
        def timeOfOperationStart = LocalDateTime.now()
        def process = DeletionProcess.builder()
                .id(1l)
                .stage(Stage.builder().code(Stage.Code.ASYNC_STAGE.name()).build())
                .build()
        def stage = Stage.Code.ASYNC_STAGE
        def actionCodes = new ArrayList<Action.Code>()
        doReturn(Stage.builder().code(Stage.Code.ASYNC_STAGE.name()).build()).when(dao).getStage(eq(Stage.Code.ASYNC_STAGE.name()))
        doReturn(Action.builder().code(Action.Code.FIRST_ACTION.name()).build()).when(dao).getAction(eq(Action.Code.FIRST_ACTION.name()))
        doReturn(ActionStatus.builder().code(ActionStatus.Code.IN_PROGRESS.name()).build()).when(dao).getActionStatus(eq(ActionStatus.Code.IN_PROGRESS.name()))
        doReturn(process).when(dao).getDeletionProcess(anyLong())
        when:
        def result = persister.persistTransition(process.getId(), stage, actionCodes)
        then:
        verify(dao, times(1)).removeAllActions(eq(process))
        verify(dao, times(1)).saveDeletionProcess(eq(process)) == null
        verify(dao, times(0)).saveActions(any()) == null
        result
        process.getStage().getCode() == Stage.Code.ASYNC_STAGE.name()
        process.getUpdated() >= timeOfOperationStart
    }
}

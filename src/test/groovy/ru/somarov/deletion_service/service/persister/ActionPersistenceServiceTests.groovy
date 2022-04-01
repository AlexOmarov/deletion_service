package ru.somarov.deletion_service.service.persister

import ru.somarov.deletion_service.Application
import ru.somarov.deletion_service.conf.TestDataSourceConfiguration
import ru.somarov.deletion_service.constant.SideSystem
import ru.somarov.deletion_service.domain.entity.*
import ru.somarov.deletion_service.domain.repository.Dao
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
class ActionPersistenceServiceTests extends Specification {

    @Autowired
    ActionPersistenceService service

    @MockBean
    Dao dao

    def "persistAction updates action with failed logic based on passed parameters and returns updated action"() {
        setup:
        long id = 1L
        ActionStatus.Code result = ActionStatus.Code.FAILED
        SideSystem system = SideSystem.FIRST_SYSTEM
        DeletionProcess process = DeletionProcess.builder().id(1L).stage(Stage.builder().code(Stage.Code.ASYNC_STAGE.name()).build()).build()
        ProcessStageAction action = ProcessStageAction.builder()
                .id(1L)
                .errorCount(1)
                .actionStatus(ActionStatus.builder().code(ActionStatus.Code.IN_PROGRESS.name()).build())
                .build()
        doReturn(process).when(dao).getProcessByClientId(anyLong())
        doReturn(action).when(dao).getProcessStageActionByProcessIdAndActionCode(anyLong(), any())
        doReturn(ActionStatus.builder().code(ActionStatus.Code.FAILED.name()).build()).when(dao).getActionStatus(eq(ActionStatus.Code.FAILED.name()))
        when:
        def persisted = service.persistAction(id, result, system)
        then:
        verify(dao, times(1)).saveAction(eq(action)) == null
        persisted.getId() == action.getId()
        persisted.getActionStatus().getCode() == ActionStatus.Code.FAILED.name()
        persisted.getErrorCount() == 2
        persisted.getErrorDescription() == "Got error from service"
    }

    def "persistAction doesn't update action when action is in final state"() {
        setup:
        long id = 1L
        ActionStatus.Code result = ActionStatus.Code.FAILED
        SideSystem system = SideSystem.FIRST_SYSTEM
        DeletionProcess process = DeletionProcess.builder().id(1L).stage(Stage.builder().code(Stage.Code.ASYNC_STAGE.name()).build()).build()
        ProcessStageAction action = ProcessStageAction.builder()
                .id(1L)
                .errorCount(1)
                .actionStatus(ActionStatus.builder().code(ActionStatus.Code.FAILED.name()).build())
                .action(Action.builder().code(Action.Code.FIRST_ACTION.name()).build())
                .build()
        doReturn(process).when(dao).getProcessByClientId(anyLong())
        doReturn(action).when(dao).getProcessStageActionByProcessIdAndActionCode(anyLong(), any())
        doReturn(ActionStatus.builder().code(ActionStatus.Code.FAILED.name()).build()).when(dao).getActionStatus(eq(ActionStatus.Code.FAILED.name()))
        when:
        def persisted = service.persistAction(id, result, system)
        then:
        verify(dao, times(0)).saveAction(eq(action)) == null
        persisted == null
    }

    def "persistAction doesn't update action when cannot find action"() {
        setup:
        long id = 1L
        ActionStatus.Code result = ActionStatus.Code.FAILED
        SideSystem system = SideSystem.FIRST_SYSTEM
        DeletionProcess process = DeletionProcess.builder().id(1L).stage(Stage.builder().code(Stage.Code.ASYNC_STAGE.name()).build()).build()
        doReturn(process).when(dao).getProcessByClientId(anyLong())
        doReturn(ActionStatus.builder().code(ActionStatus.Code.FAILED.name()).build()).when(dao).getActionStatus(eq(ActionStatus.Code.FAILED.name()))
        when:
        def persisted = service.persistAction(id, result, system)
        then:
        verify(dao, times(0)).saveAction(any()) == null
        persisted == null
    }

    def "persistAction doesn't update action when cannot find process"() {
        setup:
        long id = 1L
        ActionStatus.Code result = ActionStatus.Code.FAILED
        SideSystem system = SideSystem.FIRST_SYSTEM
        ProcessStageAction action = ProcessStageAction.builder()
                .id(1L)
                .errorCount(1)
                .actionStatus(ActionStatus.builder().code(ActionStatus.Code.FAILED.name()).build())
                .build()
        doReturn(action).when(dao).getProcessStageActionByProcessIdAndActionCode(anyLong(), any())
        doReturn(ActionStatus.builder().code(ActionStatus.Code.FAILED.name()).build()).when(dao).getActionStatus(eq(ActionStatus.Code.FAILED.name()))
        when:
        def persisted = service.persistAction(id, result, system)
        then:
        verify(dao, times(0)).saveAction(any()) == null
        persisted == null
    }


    def "persistAction (overload) updates action with succeeded logic based on passed parameters and returns updated action"() {
        setup:
        long id = 1L
        ActionStatus.Code result = ActionStatus.Code.SUCCEEDED
        Action.Code code = Action.Code.FIRST_ACTION
        DeletionProcess process = DeletionProcess.builder().id(1L).stage(Stage.builder().code(Stage.Code.ASYNC_STAGE.name()).build()).build()
        ProcessStageAction action = ProcessStageAction.builder()
                .id(1L)
                .errorCount(1)
                .actionStatus(ActionStatus.builder().code(ActionStatus.Code.IN_PROGRESS.name()).build())
                .build()
        doReturn(process).when(dao).getProcessByClientId(anyLong())
        doReturn(action).when(dao).getProcessStageActionByProcessIdAndActionCode(anyLong(), any())
        doReturn(ActionStatus.builder().code(ActionStatus.Code.SUCCEEDED.name()).build()).when(dao).getActionStatus(eq(ActionStatus.Code.SUCCEEDED.name()))
        when:
        def persisted = service.persistAction(id, result, code)
        then:
        verify(dao, times(1)).saveAction(eq(action)) == null
        persisted.getId() == action.getId()
        persisted.getActionStatus().getCode() == ActionStatus.Code.SUCCEEDED.name()
        persisted.getErrorCount() == action.getErrorCount()
    }

    def "persistAction (overload) updates action with failed logic based on passed parameters and returns updated action"() {
        setup:
        long id = 1L
        ActionStatus.Code result = ActionStatus.Code.FAILED
        Action.Code code = Action.Code.FIRST_ACTION
        DeletionProcess process = DeletionProcess.builder().id(1L).stage(Stage.builder().code(Stage.Code.ASYNC_STAGE.name()).build()).build()
        ProcessStageAction action = ProcessStageAction.builder()
                .id(1L)
                .errorCount(1)
                .actionStatus(ActionStatus.builder().code(ActionStatus.Code.IN_PROGRESS.name()).build())
                .build()
        doReturn(process).when(dao).getProcessByClientId(anyLong())
        doReturn(action).when(dao).getProcessStageActionByProcessIdAndActionCode(anyLong(), any())
        doReturn(ActionStatus.builder().code(ActionStatus.Code.FAILED.name()).build()).when(dao).getActionStatus(eq(ActionStatus.Code.FAILED.name()))
        when:
        def persisted = service.persistAction(id, result, code)
        then:
        verify(dao, times(1)).saveAction(eq(action)) == null
        persisted.getId() == action.getId()
        persisted.getActionStatus().getCode() == ActionStatus.Code.FAILED.name()
        persisted.getErrorCount() == 2
        persisted.getErrorDescription() == "Got error from service"
    }

    def "persistAction (overload) doesn't update action when action is in final state"() {
        setup:
        long id = 1L
        ActionStatus.Code result = ActionStatus.Code.FAILED
        Action.Code code = Action.Code.FIRST_ACTION
        DeletionProcess process = DeletionProcess.builder().id(1L).stage(Stage.builder().code(Stage.Code.ASYNC_STAGE.name()).build()).build()
        ProcessStageAction action = ProcessStageAction.builder()
                .id(1L)
                .errorCount(1)
                .actionStatus(ActionStatus.builder().code(ActionStatus.Code.FAILED.name()).build())
                .action(Action.builder().code(Action.Code.FIRST_ACTION.name()).build())
                .build()
        doReturn(process).when(dao).getProcessByClientId(anyLong())
        doReturn(action).when(dao).getProcessStageActionByProcessIdAndActionCode(anyLong(), any())
        doReturn(ActionStatus.builder().code(ActionStatus.Code.FAILED.name()).build()).when(dao).getActionStatus(eq(ActionStatus.Code.FAILED.name()))
        when:
        def persisted = service.persistAction(id, result, code)
        then:
        verify(dao, times(0)).saveAction(eq(action)) == null
        persisted == null
    }

    def "persistAction (overload) doesn't update action when cannot find action"() {
        setup:
        long id = 1L
        ActionStatus.Code result = ActionStatus.Code.FAILED
        Action.Code code = Action.Code.FIRST_ACTION
        DeletionProcess process = DeletionProcess.builder().id(1L).stage(Stage.builder().code(Stage.Code.ASYNC_STAGE.name()).build()).build()
        doReturn(process).when(dao).getProcessByClientId(anyLong())
        doReturn(ActionStatus.builder().code(ActionStatus.Code.FAILED.name()).build()).when(dao).getActionStatus(eq(ActionStatus.Code.FAILED.name()))
        when:
        def persisted = service.persistAction(id, result, code)
        then:
        verify(dao, times(0)).saveAction(any()) == null
        persisted == null
    }

    def "persistAction (overload) doesn't update action when cannot find process"() {
        setup:
        long id = 1L
        ActionStatus.Code result = ActionStatus.Code.FAILED
        Action.Code code = Action.Code.FIRST_ACTION
        ProcessStageAction action = ProcessStageAction.builder()
                .id(1L)
                .errorCount(1)
                .action(Action.builder().code(Action.Code.FIRST_ACTION.name()).build())
                .actionStatus(ActionStatus.builder().code(ActionStatus.Code.FAILED.name()).build())
                .build()
        doReturn(action).when(dao).getProcessStageActionByProcessIdAndActionCode(anyLong(), any())
        doReturn(ActionStatus.builder().code(ActionStatus.Code.FAILED.name()).build()).when(dao).getActionStatus(eq(ActionStatus.Code.FAILED.name()))
        when:
        def persisted = service.persistAction(id, result, code)
        then:
        verify(dao, times(0)).saveAction(any()) == null
        persisted == null
    }
}

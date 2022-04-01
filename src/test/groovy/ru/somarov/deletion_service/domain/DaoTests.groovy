package ru.somarov.deletion_service.domain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import ru.somarov.deletion_service.Application
import ru.somarov.deletion_service.conf.TestDataSourceConfiguration
import ru.somarov.deletion_service.domain.entity.*
import ru.somarov.deletion_service.domain.repository.*
import spock.lang.Specification

import static java.time.LocalDateTime.now
import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*
import static ru.somarov.deletion_service.util.TestUtils.incrementAndGet

@Import(TestDataSourceConfiguration)
@ActiveProfiles(profiles = ["test"])
@ContextConfiguration(classes = [Application])
@SpringBootTest
class DaoTests extends Specification {

    private static final WRONG = "WRONG"

    @Autowired
    Dao dao

    @SpyBean
    private CacheManager cacheManager

    @SpyBean
    private ActionRepository actionRepo

    @SpyBean
    private ActionStatusRepository actionStatusRepo

    @SpyBean
    private ClientGroupRepository clientGroupRepo

    @SpyBean
    private ClientRepository clientRepo

    @SpyBean
    private DeletionProcessRepository deletionProcessRepo

    @SpyBean
    private ProcessStageActionRepository processStageActionRepo

    @SpyBean
    private StageRepository stageRepo

    @SpyBean
    private ProcessCompletionLogRepository processCompletionLogRepo


    def setup() {
        reset(stageRepo, actionRepo, processStageActionRepo, deletionProcessRepo, clientGroupRepo, actionStatusRepo)
    }

    def cleanup() {
        cacheManager.getCacheNames().stream().forEach({ cacheName -> cacheManager.getCache(cacheName).clear() })
    }

    // Cacheable getters

    def "When we get action then cache will be populated and used next time"() {
        when:
        Action state = dao.getAction("FIRST_ACTION")
        def cache = (Action) cacheManager.getCache("action").get("code_FIRST_ACTION").get()
        then:
        state != null && state.getCode() == "FIRST_ACTION"
        cache != null && cache.getCode() == "FIRST_ACTION"
        verify(actionRepo, times(1)).findByCode("FIRST_ACTION") == null
        when:
        state = dao.getAction("FIRST_ACTION")
        then:
        state != null && state.getCode() == "FIRST_ACTION"
        verifyNoMoreInteractions(actionRepo) == null
    }

    def "When we get action with wrong name null is returned"() {
        when:
        Action state = dao.getAction(WRONG)
        def cache = (Action) cacheManager.getCache("action").get("code_" + WRONG)
        then:
        state == null
        cache == null
        verify(actionRepo, times(1)).findByCode(WRONG) == null
        when:
        state = dao.getAction(WRONG)
        then:
        state == null
        cache == null
        verify(actionRepo, times(2)).findByCode(WRONG) == null
    }

    def "When we want to get action status, then cache will be populated and used next time"() {
        when:
        ActionStatus status = dao.getActionStatus("SUCCEEDED")
        def cache = (ActionStatus) cacheManager.getCache("action_status").get("code_SUCCEEDED").get()
        then:
        status != null && status.getCode() == "SUCCEEDED"
        cache != null && cache.getCode() == "SUCCEEDED"
        verify(actionStatusRepo, times(1)).findByCode("SUCCEEDED") == null
        when:
        status = dao.getActionStatus("SUCCEEDED")
        then:
        status != null && status.getCode() == "SUCCEEDED"
        verifyNoMoreInteractions(actionStatusRepo) == null
    }

    def "When we get action status with wrong name null is returned"() {
        when:
        ActionStatus status = dao.getActionStatus(WRONG)
        def cache = (ActionStatus) cacheManager.getCache("action").get("code_" + WRONG)
        then:
        status == null
        cache == null
        verify(actionStatusRepo, times(1)).findByCode(WRONG) == null
        when:
        status = dao.getActionStatus(WRONG)
        then:
        status == null
        cache == null
        verify(actionStatusRepo, times(2)).findByCode(WRONG) == null
    }

    def "When we want to get client group, then cache will be populated and used next time"() {
        when:
        ClientGroup group = dao.getGroup("DEFAULT")
        def cache = (ClientGroup) cacheManager.getCache("client_group").get("code_DEFAULT").get()
        then:
        group != null && group.getCode() == "DEFAULT"
        cache != null && cache.getCode() == "DEFAULT"
        verify(clientGroupRepo, times(1)).findByCode("DEFAULT") == null
        when:
        group = dao.getGroup("DEFAULT")
        then:
        group != null && group.getCode() == "DEFAULT"
        verifyNoMoreInteractions(clientGroupRepo) == null
    }

    def "When we get client group with wrong name null is returned"() {
        when:
        ClientGroup group = dao.getGroup(WRONG)
        def cache = (ClientGroup) cacheManager.getCache("action").get("code_" + WRONG)
        then:
        group == null
        cache == null
        verify(clientGroupRepo, times(1)).findByCode(WRONG) == null
        when:
        group = dao.getGroup(WRONG)
        then:
        group == null
        cache == null
        verify(clientGroupRepo, times(2)).findByCode(WRONG) == null
    }

    def "When we want to get stage, then cache will be populated and used next time"() {
        when:
        Stage stage = dao.getStage("FIRST_STAGE")
        def cache = (Stage) cacheManager.getCache("stage").get("code_FIRST_STAGE").get()
        then:
        stage != null && stage.getCode() == "FIRST_STAGE"
        cache != null && cache.getCode() == "FIRST_STAGE"
        verify(stageRepo, times(1)).findByCode("FIRST_STAGE") == null
        when:
        stage = dao.getStage("FIRST_STAGE")
        then:
        stage != null && stage.getCode() == "FIRST_STAGE"
        verifyNoMoreInteractions(stageRepo) == null
    }

    def "When we get stage with wrong name null is returned"() {
        when:
        Stage stage = dao.getStage(WRONG)
        def cache = (Stage) cacheManager.getCache("action").get("code_" + WRONG)
        then:
        stage == null
        cache == null
        verify(stageRepo, times(1)).findByCode(WRONG) == null
        when:
        stage = dao.getStage(WRONG)
        then:
        stage == null
        cache == null
        verify(stageRepo, times(2)).findByCode(WRONG) == null
    }

    // Simple getters


    def "When we call getDeletionProcess then process info is returned if process exists"() {
        setup:
        def id = incrementAndGet()
        def updated = now()

        def client = dao.saveClient(Client.builder().id(id).group(ClientGroup.builder().id(1L).code("DEFAULT").build()).build())

        def savedProcess = dao.saveDeletionProcess(DeletionProcess.builder().client(client).updated(updated)
                .stage(Stage.builder().id(9L).code("STARTED").build()).build())
        when:
        def process = dao.getDeletionProcess(savedProcess.getId())
        then:
        process.getClient().getId() == id
        process.getStage().getCode() == "STARTED"
        // Lst 3 digits after dot (microseconds) are thrown out before storing in the database
        process.getUpdated().toLocalDate() == updated.toLocalDate()
    }

    def "When process doesn't exist getDeletionProcess returns null"() {
        setup:
        def id = incrementAndGet()
        when:
        def process = dao.getDeletionProcess(id)
        then:
        process == null
    }

    def "When action doesn't exist getProcessStageActionByProcessIdAndActionCode returns null"() {
        setup:
        def id = incrementAndGet()
        def updated = now()

        def client = dao.saveClient(Client.builder().id(id).group(ClientGroup.builder().id(1L)
                .code("DEFAULT").build()).build())

        def savedProcess = dao.saveDeletionProcess(
                DeletionProcess.builder()
                        .client(client)
                        .updated(updated)
                        .stage(Stage.builder().id(9L).code("STARTED").build())
                        .build()
        )
        when:
        def action = dao.getProcessStageActionByProcessIdAndActionCode(savedProcess.getId(), Action.Code.FIRST_ACTION)
        then:
        action == null
    }

    // Simple savers

    def "Dao saves client when saveClient is called"() {
        setup:
        reset(clientRepo)
        doReturn(null).when(clientRepo).save(nullable(Client.class))
        when:
        dao.saveClient(null)
        then:
        verify(clientRepo, times(1)).save(nullable(Client.class)) == null
        cleanup:
        reset(clientRepo)
    }

    def "Dao saves process completion log when saveProcessCompletionLog is called"() {
        setup:
        reset(processCompletionLogRepo)
        doReturn(null).when(processCompletionLogRepo).save(nullable(ProcessCompletionLog.class))
        when:
        dao.saveProcessCompletionLog(null)
        then:
        verify(processCompletionLogRepo, times(1)).save(nullable(ProcessCompletionLog.class)) == null
        cleanup:
        reset(processCompletionLogRepo)
    }

    def "Dao saves process when saveDeletionProcess is called"() {
        setup:
        reset(deletionProcessRepo)
        doReturn(null).when(deletionProcessRepo).save(nullable(DeletionProcess.class))
        when:
        dao.saveDeletionProcess(null)
        then:
        verify(deletionProcessRepo, times(1)).save(nullable(DeletionProcess.class)) == null
    }

    def "Dao saves actions when saveActions is called"() {
        setup:
        reset(processStageActionRepo)
        doReturn(null).when(processStageActionRepo).saveAll(nullable(List<ProcessStageAction>.class))
        when:
        dao.saveActions(null)
        then:
        verify(processStageActionRepo, times(1)).saveAll(nullable(List<ProcessStageAction>.class)) == null
    }

    def "Dao saves action when saveAction is called"() {
        setup:
        reset(processStageActionRepo)
        doReturn(null).when(processStageActionRepo).save(nullable(ProcessStageAction.class))
        when:
        dao.saveAction(null)
        then:
        verify(processStageActionRepo, times(1)).save(nullable(ProcessStageAction.class)) == null
    }

    // Simple removers

    def "Dao removes all actions when removeAllActions is called"() {
        setup:
        doNothing().when(processStageActionRepo).deleteAllByProcessId(nullable(Long.class))
        when:
        dao.removeAllActions(DeletionProcess.builder().id(1L).build())
        then:
        verify(processStageActionRepo, times(1)).deleteAllByProcessId(nullable(Long.class)) == null

    }

    // Native requests


    def "startDeletionProcess saves deletion process if args are valid"() {
        setup:
        long id = 1L
        def group = "DEFAULT"
        when:
        dao.startDeletionProcess(id, group)
        then:
        verify(deletionProcessRepo, times(1)).save(any()) == null
    }

    def "removeProcess calls deletion process repo to remove passed process"() {
        when:
        dao.removeProcess(1L)
        then:
        verify(deletionProcessRepo, times(1)).deleteById(eq(1L))
    }
}

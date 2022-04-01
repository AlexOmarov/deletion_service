package ru.somarov.deletion_service.domain.repository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.somarov.deletion_service.domain.entity.*;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

/**
 * This class is a proxy between service layer and database layer (representing in repos)
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Dao {

    private final EntityManager manager;

    private final ActionRepository actionRepo;
    private final ActionStatusRepository actionStatusRepo;
    private final ClientGroupRepository clientGroupRepo;
    private final ClientRepository clientRepo;
    private final DeletionProcessRepository deletionProcessRepo;
    private final ProcessStageActionRepository processStageActionRepo;
    private final StageRepository stageRepo;
    private final ProcessCompletionLogRepository processCompletionRepo;

    @Value("${app.action.retry.attempts}")
    private Integer maxAttempts;
    @Value("${app.action.retry.timeout}")
    private Long timeout;

    // Cacheable getters

    @Cacheable(value = "action", key = "'code_' + #code", unless = "#result == null")
    public Action getAction(String code) {
        return actionRepo.findByCode(code);
    }

    @Cacheable(value = "action_status", key = "'code_' + #code", unless = "#result == null")
    public ActionStatus getActionStatus(String code) {
        return actionStatusRepo.findByCode(code);
    }

    @Cacheable(value = "client_group", key = "'code_' + #code", unless = "#result == null")
    public ClientGroup getGroup(String code) {
        return clientGroupRepo.findByCode(code);
    }

    @Cacheable(value = "stage", key = "'code_' + #code", unless = "#result == null")
    public Stage getStage(String code) {
        return stageRepo.findByCode(code);
    }

    // Simple getters

    @Transactional
    public DeletionProcess getDeletionProcess(long id) {
        return deletionProcessRepo.findById(id);
    }

    public DeletionProcess getProcessByClientId(long clientId) {
        return deletionProcessRepo.findByClientId(clientId);
    }

    @Transactional
    public ProcessStageAction getProcessStageActionByProcessIdAndActionCode(Long id, Action.Code code) {
        return processStageActionRepo.findByProcessIdAndActionCode(id, code.name());
    }

    public List<ProcessStageAction> getFailedActions(long id) {
        return processStageActionRepo.findByProcessIdAndErrorCountGreaterThanEqualAndActionStatusCode(id, maxAttempts, ActionStatus.Code.FAILED.name());
    }


    public List<ProcessStageAction> getActions(long id) {
        return processStageActionRepo.findByProcessId(id);
    }

    @Transactional
    public Page<ProcessStageAction> getActionsForRetry(Pageable request) {
        return processStageActionRepo.findByErrorCountLessThanAndActionStatusCodeOrErrorCountLessThanEqualAndActionStatusCodeAndUpdatedLessThanOrderByUpdatedAsc(
                maxAttempts, ActionStatus.Code.FAILED.name(),
                maxAttempts, ActionStatus.Code.IN_PROGRESS.name(),
                LocalDateTime.now().minusMinutes(timeout), request);
    }

    // Simple savers

    public Client saveClient(Client client) {
        return clientRepo.save(client);
    }

    //@Transactional
    public ProcessCompletionLog saveProcessCompletionLog(ProcessCompletionLog log) {
        return processCompletionRepo.save(log);
    }

    public void saveActions(List<ProcessStageAction> actions) {
        processStageActionRepo.saveAll(actions);
    }

    public ProcessStageAction saveAction(ProcessStageAction action) {
        return processStageActionRepo.save(action);
    }

    // Will throw exception if process for client with such type already exists
    public DeletionProcess startDeletionProcess(long id, @NonNull String group) {
        return saveDeletionProcess(DeletionProcess.builder()
                .client(createClient(id, group))
                .stage(getStage(Stage.Code.STARTED.name()))
                .updated(LocalDateTime.now())
                .build());
    }

    // Simple removers

    public void removeAllActions(DeletionProcess process) {
        if (process == null || process.getId() == null) {
            log.error("Cannot remove actions for null process without id");
            return;
        }
        processStageActionRepo.deleteAllByProcessId(process.getId());
    }

    public void removeProcess(long id) {
        deletionProcessRepo.deleteById(id);
    }

    // Native requests

    /**
     * Function saves deletion process if there are no such process for client yet
     *
     * @param process process info
     * @return DeletionProcess process info
     * @since 1.0.0
     *
     */
    public DeletionProcess saveDeletionProcess(DeletionProcess process) {
        return deletionProcessRepo.save(process);
    }

    // Private methods

    private Client createClient(long id, String group) {
        log.debug("Got creation client request for id - {}, broker - {}", id, group);
        return saveClient(Client.builder().id(id).group(getGroup(group)).build());
    }

}

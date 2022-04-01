package ru.somarov.deletion_service.service.persister;

import ru.somarov.deletion_service.domain.entity.*;
import ru.somarov.deletion_service.domain.repository.Dao;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a persister for each SM transition
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @see Dao
 *
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransitionPersistenceService {

    private final Dao dao;

    /**
     * Function stores transition info in the database by updating underlying deletion process and it's actions
     *
     * @param id          Id of Underlying deletion process
     * @param stage       Target stage of transition, new stage of underlying process
     * @param actionCodes List of action that should be persisted for new stage
     * @return boolean    Has persistence operation been successfully completed
     * @since 1.0.0
     *
     */
    @Transactional
    public DeletionProcess persistTransition(long id, @NonNull Stage.Code stage, @NonNull List<Action.Code> actionCodes) {
        // If Process has not been set to next stage in parallel
        log.debug("Persistence of transition for process {} with stage {} was requested", id, stage);
        var process = dao.getDeletionProcess(id);
        if (!stage.name().equalsIgnoreCase(process.getStage().getCode())) {
            process.setStage(dao.getStage(stage.name()));
            process.setUpdated(LocalDateTime.now());
            dao.removeAllActions(process);
            dao.saveDeletionProcess(process);
            List<ProcessStageAction> actions = new ArrayList<>();
            actionCodes.forEach(code -> actions.add(getAction(code.name(), process)));
            if (!actions.isEmpty()) {
                dao.saveActions(actions);
            }
            log.debug("Persistence of transition for process {} with stage {} was completed", id, stage);
            return process;
        }
        log.warn("Persistence of transition for process {} with stage {} was not completed - process has already been in this stage", id, stage);
        return null;
    }

    private ProcessStageAction getAction(String actionCode, DeletionProcess process) {
        return ProcessStageAction.builder()
                .action(dao.getAction(actionCode))
                .actionStatus(dao.getActionStatus(ActionStatus.Code.IN_PROGRESS.name()))
                .errorCount(0)
                .errorDescription("")
                .process(process)
                .updated(LocalDateTime.now())
                .build();
    }
}

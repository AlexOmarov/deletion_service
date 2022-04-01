package ru.somarov.deletion_service.service.persister;

import ru.somarov.deletion_service.constant.SideSystem;
import ru.somarov.deletion_service.domain.entity.*;
import ru.somarov.deletion_service.domain.repository.Dao;
import ru.somarov.deletion_service.props.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class is a persister for each action
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 * @see Dao
 * @see AppProperties
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActionPersistenceService {

    private final Dao dao;
    private final AppProperties props;

    @Transactional
    public ProcessStageAction persistAction(long clientId, ActionStatus.Code result,
                                            SideSystem system) {
        log.debug("Trying to persist action for client {} with status {}, producer {}",
                clientId, result, system);
        DeletionProcess process = dao.getProcessByClientId(clientId);
        if (process == null) {
            log.error("Got completion for action of process that does not exist: client - {}, system - {}, result - {}",
                    clientId, system, result);
            return null;
        }
        var actionCode = props.getActionsConfig().get(Stage.Code.valueOf(process.getStage().getCode())).get(system);
        return persist(process.getId(), result, actionCode);
    }

    @Transactional
    public ProcessStageAction persistAction(long processId, ActionStatus.Code result,
                                            Action.Code actionCode) {
        log.debug("Trying to persist action for process {} with status {}, code {}",
                processId, result, actionCode);
        return persist(processId, result, actionCode);
    }

    private ProcessStageAction persist(long processId, ActionStatus.Code result, Action.Code actionCode) {
        var action = dao.getProcessStageActionByProcessIdAndActionCode(processId, actionCode);
        if (action == null) {
            log.warn("Got completion for action that does not exist: process - {}, code - {}, result - {}", processId, actionCode, result);
            return null;
        }
        if (!ActionStatus.Code.IN_PROGRESS.name().equalsIgnoreCase(action.getActionStatus().getCode())) {
            log.warn("Got completion for action {} that is already in final {} state: process - {}, code - {}, result - {}", action.getAction().getCode(),
                    action.getActionStatus().getCode(), processId, actionCode, result);
            return null;
        }

        if (ActionStatus.Code.FAILED.equals(result)) {
            Integer errorCount = action.getErrorCount();
            action.setErrorCount(++errorCount);
            action.setErrorDescription("Got error from service");
        }
        action.setActionStatus(dao.getActionStatus(result.name()));
        dao.saveAction(action);
        return action;
    }
}

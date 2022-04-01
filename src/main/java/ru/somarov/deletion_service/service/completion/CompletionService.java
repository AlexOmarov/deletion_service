package ru.somarov.deletion_service.service.completion;

import ru.somarov.deletion_service.constant.ResultCode;
import ru.somarov.deletion_service.constant.SideSystem;
import ru.somarov.deletion_service.constant.state_machine.SmEvent;
import ru.somarov.deletion_service.domain.entity.*;
import ru.somarov.deletion_service.domain.repository.Dao;
import ru.somarov.deletion_service.service.persister.ActionPersistenceService;
import ru.somarov.deletion_service.service.state_machine.StateMachineService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

import static ru.somarov.deletion_service.utils.Utils.nextEvent;

/**
 * Service for working with async action and process completions
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompletionService {

    @Value("${app.action.retry.attempts}")
    private Integer maxAttempts;

    private final StateMachineService service;
    private final ActionPersistenceService persistenceService;
    private final Dao dao;

    /**
     * Function completes any action (async or sync)
     *
     * @param clientId   id of action's client
     * @param status     result of action processing
     * @param systemCode code of system which has processed action
     * @since 1.0.0
     *
     */
    public void completeAction(long clientId, @NonNull String status, @NonNull String systemCode) {
        var system = SideSystem.valueOf(systemCode.toUpperCase());
        ActionStatus.Code result = switch (ResultCode.valueOf(status.toUpperCase())) {
            case SUCCEEDED, ALREADY_DONE -> ActionStatus.Code.SUCCEEDED;
            case ERROR -> ActionStatus.Code.FAILED;
        };
        ProcessStageAction action = persistenceService.persistAction(clientId, result, system);
        if (action == null) {
            log.error("Cannot complete action with args: client {}, status {}, system {}", clientId, status, systemCode);
            return;
        }
        DeletionProcess process = action.getProcess();
        // Check process for completion of all actions in terms of current stage
        checkAndSendNextEvent(process);
    }

    /**
     * Function checks list of action statuses and sends next event if all of them are completed
     *
     * @param process process that should be checked (are all actions completed or not)
     * @since 1.0.0
     *
     */
    public void checkAndSendNextEvent(@NonNull DeletionProcess process) {
        SmEvent event = nextEvent(
                Stage.Code.valueOf(process.getStage().getCode()),
                dao.getActions(
                        process.getId()).stream().map(action -> Pair.of(ActionStatus.Code.valueOf(action.getActionStatus().getCode()), action.getErrorCount())
                ).collect(Collectors.toList()),
                maxAttempts
        );
        if (event != null) {
            service.sendNextEvent(process, event);
        }
    }
}

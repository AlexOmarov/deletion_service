package ru.somarov.deletion_service.service.retry;

import ru.somarov.deletion_service.domain.entity.Action;
import ru.somarov.deletion_service.domain.entity.ActionStatus;
import ru.somarov.deletion_service.domain.entity.DeletionProcess;
import ru.somarov.deletion_service.domain.entity.ProcessStageAction;
import ru.somarov.deletion_service.domain.repository.Dao;
import ru.somarov.deletion_service.service.action.ActionHandlerService;
import ru.somarov.deletion_service.service.completion.CompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a service for performing any retry functionality
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @see ActionHandlerService
 * @see CompletionService
 * @see Dao
 *
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetryService {

    private final Dao dao;
    private final ActionHandlerService service;
    private final CompletionService completionService;

    @Value("${app.action.retry.attempts}")
    private Integer maxAttempts;
    @Value("${app.action.retry.timeout}")
    private Long timeout;

    /**
     * Function performs retry upon limited set of retryable actions
     *
     * @param request Limit of actions that can be retried
     * @return Pair<Boolean, List <DeletionProcess>> are there actions for retry or all of them
     *                                                was processed and list of retried processes
     * @since 1.0.0
     *
     */
    @Transactional
    public Pair<Boolean, List<DeletionProcess>> retry(Pageable request) {
        Page<ProcessStageAction> retryActions = dao.getActionsForRetry(request);
        List<DeletionProcess> processesToUpdate = new ArrayList<>();
        retryActions.getContent().forEach(action -> {
            // If action is retried with IN_PROGRESS status we should increment error count (as timeout error occurred)
            if(action.getActionStatus().getCode().equalsIgnoreCase(ActionStatus.Code.IN_PROGRESS.name())) {
                action.setErrorCount(action.getErrorCount() + 1);
            }
            ActionStatus.Code status;
            if (action.getErrorCount() >= maxAttempts && action.getUpdated().isBefore(LocalDateTime.now().minusMinutes(timeout))
                    && action.getActionStatus().getCode().equalsIgnoreCase(ActionStatus.Code.IN_PROGRESS.name())) {
                status = ActionStatus.Code.FAILED;
            } else {
                status = service.handle(action.getProcess(), Action.Code.valueOf(action.getAction().getCode()));
                if(status.equals(ActionStatus.Code.FAILED)) {
                    action.setErrorCount(action.getErrorCount() + 1);
                }
            }

            if (status == ActionStatus.Code.FAILED) {
                action.setErrorDescription("Got error from action handler");
            } else {
                action.setErrorDescription("Retry has been performed");
            }

            action.setActionStatus(dao.getActionStatus(status.name()));
            action.setUpdated(LocalDateTime.now());
            if (status != ActionStatus.Code.IN_PROGRESS) {
                processesToUpdate.add(action.getProcess());
            }
        });
        dao.saveActions(retryActions.getContent());
        return Pair.of(retryActions.hasNext(), processesToUpdate);
    }

    public void checkForCompletion(List<DeletionProcess> processes) {
        processes.forEach(completionService::checkAndSendNextEvent);
    }
}

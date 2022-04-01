package ru.somarov.deletion_service.service.state_machine;

import ru.somarov.deletion_service.constant.state_machine.SmEvent;
import ru.somarov.deletion_service.domain.entity.*;
import ru.somarov.deletion_service.domain.entity.jsonb.Completion;
import ru.somarov.deletion_service.domain.repository.Dao;
import ru.somarov.deletion_service.props.AppProperties;
import ru.somarov.deletion_service.service.action.ActionHandlerService;
import ru.somarov.deletion_service.service.persister.TransitionPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ru.somarov.deletion_service.utils.Utils.nextEvent;

/**
 * Service for performing logic before transition occurs.
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransitionService {

    private static final String TRANSITION_ERROR_MSG = "Process {} with stage {}: Got event {}, " +
            "which should transit process to next stage, but transition has not been done";

    private static final String NO_ACTIONS_TO_PERFORM = "Got no actions to perform after trying to change state of " +
            "process {} to {}";

    @Value("${app.action.retry.attempts}")
    private Integer maxAttempts;

    private final Dao dao;
    private final TransitionPersistenceService persister;
    private final ActionHandlerService actionHandlerService;
    private final AppProperties props;

    /**
     * Function performs transition to any of non-final states
     *
     * @param context Context of state machine transition
     * @since 1.0.0
     *
     */
    public void transit(StateContext<Stage.Code, SmEvent> context) {
        var id = Long.parseLong(context.getStateMachine().getId());
        Stage.Code target = context.getTarget().getId();
        log.debug("Transit method was called for process {} for stage {}", id, target);
        List<Action.Code> codes = props.getActionsForStages().get(target);
        DeletionProcess process = persister.persistTransition(id, target, codes);
        List<Pair<ActionStatus.Code, Integer>> results = new ArrayList<>();
        if (process != null) {
            if (codes.isEmpty()) {
                log.debug(NO_ACTIONS_TO_PERFORM, id, target);
            } else {
                // TODO: save actions in final status
                codes.forEach(code -> results.add(Pair.of(actionHandlerService.handle(process, code),0)));
                SmEvent next = nextEvent(target, results, maxAttempts);
                if(next != null) {
                    context.getExtendedState().getVariables().put("next", next);
                }
                context.getExtendedState().getVariables().put("process", process);
            }
        } else {
            log.error(TRANSITION_ERROR_MSG, id, target, context.getEvent());
        }
    }

    /**
     * Function performs transition to succeeded state
     *
     * @param context Context of state machine transition
     * @since 1.0.0
     *
     */
    public void complete(StateContext<Stage.Code, SmEvent> context) {
        var id = Long.parseLong(context.getStateMachine().getId());
        Stage.Code target = context.getTarget().getId();
        log.debug("Succeeded method was called for process {} for stage {}", id, target);
        List<Action.Code> codes = props.getActionsForStages().get(target);
        DeletionProcess process = persister.persistTransition(id, target, codes);
        List<ProcessStageAction> failedActions = dao.getFailedActions(id);
        if (process != null) {
            finishProcessing(id, target, codes, process, failedActions);
        } else {
            log.error(TRANSITION_ERROR_MSG, id, target, context.getEvent());
        }
    }

    private void finishProcessing(long id, Stage.Code target,
                                  List<Action.Code> codes,
                                  DeletionProcess process,
                                  List<ProcessStageAction> failedActions) {
        if (codes.isEmpty()) {
            log.debug(NO_ACTIONS_TO_PERFORM, id, target);
        } else {
            codes.forEach(code -> actionHandlerService.handle(process, code));
        }
        dao.saveProcessCompletionLog(ProcessCompletionLog.builder()
                .id(UUID.randomUUID())
                .created(LocalDateTime.now())
                .completion(Completion.builder().process(process)
                        .failedActions(failedActions).build()).build());
        dao.removeProcess(id);
    }

}

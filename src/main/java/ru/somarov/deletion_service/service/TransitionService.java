package ru.somarov.deletion_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Service;
import ru.somarov.deletion_service.constant.SmEvent;
import ru.somarov.deletion_service.constant.SmState;

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

    /**
     * Function performs transition to any of non-final states
     *
     * @param context Context of state machine transition
     * @since 1.0.0
     *
     */
    public void transit(StateContext<SmState, SmEvent> context) {
        var id = Long.parseLong(context.getStateMachine().getId());
        SmState target = context.getTarget().getId();
        log.debug("Transit method was called for process {} for stage {}", id, target);
    }

    /**
     * Function performs transition to succeeded state
     *
     * @param context Context of state machine transition
     * @since 1.0.0
     *
     */
    public void complete(StateContext<SmState, SmEvent> context) {
        var id = Long.parseLong(context.getStateMachine().getId());
        SmState target = context.getTarget().getId();
        log.debug("Complete method was called for process {} for stage {}", id, target);
    }

}

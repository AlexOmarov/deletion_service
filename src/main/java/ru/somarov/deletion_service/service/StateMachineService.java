package ru.somarov.deletion_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.stereotype.Service;
import ru.somarov.deletion_service.constant.SmEvent;
import ru.somarov.deletion_service.constant.SmState;

import java.util.HashMap;
import java.util.UUID;

/**
 * Service for working with state machine functionalities
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StateMachineService {

    private static final String COMPLETION_MESSAGE = "Trying to send event {} for process {} with stage {}";

    private final StateMachineFactory<SmState, SmEvent> factory;

    public void send(StateMachine<SmState, SmEvent> sm, SmEvent event) {
        log.debug(COMPLETION_MESSAGE, event, sm.getId(), sm.getState().getId());
        sm.sendEvent(new GenericMessage<>(event, new HashMap<>()));
    }

    public StateMachine<SmState, SmEvent> build(UUID id, SmState state) {
        log.debug("Building state machine for process {} in stage {}", id, state);
        StateMachine<SmState, SmEvent> sm = factory.getStateMachine(id.toString());

        sm.stopReactively().subscribe();

        // Here we perform a reset of state machine,
        // so we need to reassign all interceptors / contexts to it
        // Maybe we can do it better
        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                            sma.resetStateMachineReactively(new DefaultStateMachineContext<>(state, null, null, null, null, id.toString())).subscribe();
                            sma.addStateMachineInterceptor(
                                    new StateMachineInterceptorAdapter<>() {
                                        @Override
                                        public Exception stateMachineError(StateMachine<SmState, SmEvent> stateMachine,
                                                                           Exception exception) {
                                            log.error("Got exception during processing state machine {} transition", stateMachine.getId(), exception);
                                            return exception;
                                        }
                                    });
                        }
                );
        sm.startReactively().subscribe();
        return sm;
    }
}

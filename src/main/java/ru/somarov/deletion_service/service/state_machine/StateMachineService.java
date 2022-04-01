package ru.somarov.deletion_service.service.state_machine;

import ru.somarov.deletion_service.constant.state_machine.SmEvent;
import ru.somarov.deletion_service.domain.entity.DeletionProcess;
import ru.somarov.deletion_service.domain.entity.Stage;
import ru.somarov.deletion_service.domain.repository.Dao;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.stereotype.Service;

import java.util.HashMap;

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

    private final StateMachineFactory<Stage.Code, SmEvent> factory;
    private final Dao dao;

    /**
     * Function creates deletion process, builds state machine upon it and sends first event
     *
     * @param clientId id of client
     * @param event    first event to send to sm
     * @param group    Group of client
     * @since 1.0.0
     *
     */
    public void start(long clientId, @NonNull SmEvent event, @NonNull String group) {
        sendNextEvent(startDeletionProcess(clientId, group), event);
    }

    public void sendNextEvent(DeletionProcess process, SmEvent event) {
        StateMachine<Stage.Code, SmEvent> sm = build(process);
        log.debug(COMPLETION_MESSAGE, event, sm.getId(), sm.getState().getId());
        sm.sendEvent(new GenericMessage<>(event, new HashMap<>()));
        SmEvent next = sm.getExtendedState().get("next", SmEvent.class);
        sm.getExtendedState().getVariables().remove("next");
        if (next != null) {
            log.debug("Got next completion event while executing logic of previous event {}: " + COMPLETION_MESSAGE,
                    event, next, sm.getId(), sm.getState().getId());
            sendNextEvent(sm.getExtendedState().get("process", DeletionProcess.class), next);
        }
    }

    private StateMachine<Stage.Code, SmEvent> build(DeletionProcess process) {
        log.debug("Building state machine for process {} in stage {}", process.getId(), process.getStage().getCode());
        StateMachine<Stage.Code, SmEvent> sm = factory.getStateMachine(Long.toString(process.getId()));

        sm.stopReactively().subscribe();

        // Here we perform a reset of state machine,
        // so we need to reassign all interceptors / contexts to it
        // Maybe we can do it better
        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                            sma.resetStateMachineReactively(new DefaultStateMachineContext<>(
                                    Stage.Code.valueOf(process.getStage().getCode()),
                                    null, null, null, null,
                                    Long.toString(process.getId()))).subscribe();
                            sma.addStateMachineInterceptor(
                                    new StateMachineInterceptorAdapter<>() {
                                        @Override
                                        public Exception stateMachineError(StateMachine<Stage.Code, SmEvent> stateMachine,
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

    private DeletionProcess startDeletionProcess(long id,
                                                 @NonNull String group) {
        log.debug("Starting deletion process for client {}, broker {}", id, group);
        return dao.startDeletionProcess(id, group);
    }
}

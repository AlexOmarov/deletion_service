package ru.somarov.deletion_service.config;

import ru.somarov.deletion_service.constant.state_machine.SmEvent;
import ru.somarov.deletion_service.domain.entity.Stage;
import ru.somarov.deletion_service.service.state_machine.TransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

import static ru.somarov.deletion_service.domain.entity.Stage.Code.*;

/**
 * This class is a state machine config.
 * Responsible for enabling SM capabilities across application and tuning this machine.
 * This config registers states, events and post-transition actions for SM in a declarative form
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableStateMachineFactory
public class StateMachineConfig extends StateMachineConfigurerAdapter<Stage.Code, SmEvent> {
    
    private static final String ERROR_MSG = "Got exception from SM: {}";

    /**
     * Service which holds all business logic which is performed before transition has been completed
     */
    private final TransitionService service;

    /**
     * Procedure registers all states which state machine can be transited to
     *
     * @param states Configurer of SM states
     * @since 1.0.0
     *
     */
    @Override
    public void configure(StateMachineStateConfigurer<Stage.Code, SmEvent> states) throws Exception {
        states.withStates().initial(STARTED).states(EnumSet.allOf(Stage.Code.class)).end(COMPLETED);
    }

    /**
     * Procedure registers all transitions SM can perform
     * Remember, that all action()s are executed BEFORE SM changes state, so if we send event to change state further
     * INSIDE any action then business logic will be performed, but the state of state machine object will not change!
     *
     * @param transitions Configurer of SM transitions
     * @since 1.0.0
     *
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<Stage.Code, SmEvent> transitions) throws Exception {
        transitions

                // STAGE 1 (Web request):
                // Change stage to checking and perform check
                .withExternal().source(STARTED).target(WEB_REQUEST_STAGE)
                .event(SmEvent.WEB_REQUEST_STARTED).action(service::transit, context -> log.error(ERROR_MSG, context.getException().getMessage()))
                // Check has been rejected
                .and()
                .withExternal().source(WEB_REQUEST_STAGE).target(COMPLETED)
                .event(SmEvent.WEB_REQUEST_REJECTED).action(service::complete, context -> log.error(ERROR_MSG, context.getException().getMessage()))
                // Check has been failed, set stage to failed
                .and()
                .withExternal().source(WEB_REQUEST_STAGE).target(COMPLETED)
                .event(SmEvent.WEB_REQUEST_FAILED).action(service::complete, context -> log.error(ERROR_MSG, context.getException().getMessage()))
                // Check has been passed, perform block
                .and()
                .withExternal().source(WEB_REQUEST_STAGE).target(ASYNC_STAGE)
                .event(SmEvent.WEB_REQUEST_SUCCEEDED).action(service::transit, context -> log.error(ERROR_MSG, context.getException().getMessage()))

                // STAGE 2 (Blocking):
                // Block has been completed with error, set status to failed
                .and()
                .withExternal().source(ASYNC_STAGE).target(COMPLETED)
                .event(SmEvent.ASYNC_ERROR).action(service::complete, context -> log.error(ERROR_MSG, context.getException().getMessage()))
                // Block has been completed successfully, perform logout
                .and()
                .withExternal().source(ASYNC_STAGE).target(COMPLETED)
                .event(SmEvent.ASYNC_SUCCEEDED).action(service::transit, context -> log.error(ERROR_MSG, context.getException().getMessage()));

    }
}

package ru.somarov.deletion_service.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.messaging.support.GenericMessage
import org.springframework.statemachine.StateMachine
import org.springframework.statemachine.config.StateMachineFactory
import org.springframework.statemachine.support.DefaultStateMachineContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import reactor.core.publisher.Mono
import ru.somarov.deletion_service.Application
import ru.somarov.deletion_service.constant.SmEvent
import ru.somarov.deletion_service.constant.SmState
import ru.somarov.deletion_service.service.TransitionService
import spock.lang.Specification

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

@ActiveProfiles(profiles = ["test"])
@ContextConfiguration(classes = [Application])
@SpringBootTest
class StateMachineConfigTests extends Specification {

    @Autowired
    StateMachineFactory<SmState, SmEvent> smFactory

    @MockBean
    TransitionService service

    def "Spring state machine is created in status STARTED"() {
        when:
        StateMachine<SmState, SmEvent> sm = smFactory.getStateMachine("processId")
        sm.startReactively().subscribe()
        then:
        sm.getState().getId() == SmState.STARTED
    }

    def "Spring state machine transits from STARTED to WEB_REQUEST_STAGE due to WEB_REQUEST_STARTED event"() {
        setup:
        StateMachine<SmState, SmEvent> sm = smFactory.getStateMachine("processId")
        sm.startReactively().subscribe()
        when:
        sm.sendEvent(Mono.just(new GenericMessage<SmEvent>(SmEvent.PROCESS_STARTED)))
                .doOnComplete(result -> println("Mono completed!"))
                .subscribe()
        then:
        verify(service, times(1)).transit(any())
        sm.getState().getId() == SmState.IN_PROGRESS
    }

    def "Spring state machine transits from CHECKING to ASYNC_STAGE due to WEB_REQUEST_SUCCEEDED event"() {
        setup:
        StateMachine<SmState, SmEvent> sm = smFactory.getStateMachine("processId")
        init(SmState.IN_PROGRESS, sm)
        when:
        sm.sendEvent(Mono.just(new GenericMessage<SmEvent>(SmEvent.PROCESS_SUCCEEDED)))
                .doOnComplete(result -> println("Mono completed!"))
                .subscribe()
        then:
        verify(service, times(1)).complete(any())
        sm.getState().getId() == SmState.COMPLETED
    }

    def "Spring state machine transits from CHECKING to COMPLETED due to WEB_REQUEST_REJECTED event"() {
        setup:
        StateMachine<SmState, SmEvent> sm = smFactory.getStateMachine("processId")
        init(SmState.IN_PROGRESS, sm)
        when:
        sm.sendEvent(Mono.just(new GenericMessage<SmEvent>(SmEvent.PROCESS_FAILED)))
                .doOnComplete(result -> println("Mono completed!"))
                .subscribe()
        then:
        verify(service, times(1)).complete(any())
        sm.getState().getId() == SmState.COMPLETED
    }

    private void init(SmState initState, StateMachine<SmState, SmEvent> sm) {
        sm.stopReactively().subscribe()

        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> sma.resetStateMachineReactively(
                        new DefaultStateMachineContext<>(initState, null, null, null, null, "1")
                ).subscribe())

        sm.startReactively().subscribe()
    }


}

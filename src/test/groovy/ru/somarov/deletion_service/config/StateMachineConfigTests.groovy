package ru.somarov.deletion_service.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.messaging.support.GenericMessage
import org.springframework.statemachine.StateMachine
import org.springframework.statemachine.config.StateMachineFactory
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import reactor.core.publisher.Mono
import ru.somarov.deletion_service.Application
import ru.somarov.deletion_service.conf.TestDataSourceConfiguration
import ru.somarov.deletion_service.constant.state_machine.SmEvent
import ru.somarov.deletion_service.domain.entity.Stage
import ru.somarov.deletion_service.service.state_machine.TransitionService
import spock.lang.Specification

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static ru.somarov.deletion_service.util.TestUtils.init

@Import(TestDataSourceConfiguration)
@ActiveProfiles(profiles = ["test"])
@ContextConfiguration(classes = [Application])
@SpringBootTest
class StateMachineConfigTests extends Specification {

    @Autowired
    StateMachineFactory<Stage.Code, SmEvent> smFactory

    @MockBean
    TransitionService service

    def "Spring state machine is created in status STARTED"() {
        when:
        StateMachine<Stage.Code, SmEvent> sm = smFactory.getStateMachine("processId")
        sm.startReactively().subscribe()
        then:
        sm.getState().getId() == Stage.Code.STARTED
    }

    def "Spring state machine transits from STARTED to WEB_REQUEST_STAGE due to WEB_REQUEST_STARTED event"() {
        setup:
        StateMachine<Stage.Code, SmEvent> sm = smFactory.getStateMachine("processId")
        sm.startReactively().subscribe()
        when:
        sm.sendEvent(Mono.just(new GenericMessage<SmEvent>(SmEvent.WEB_REQUEST_STARTED)))
                .doOnComplete(result -> println("Mono completed!"))
                .subscribe()
        then:
        verify(service, times(1)).transit(any())
        sm.getState().getId() == Stage.Code.WEB_REQUEST_STAGE
    }

    def "Spring state machine transits from CHECKING to ASYNC_STAGE due to WEB_REQUEST_SUCCEEDED event"() {
        setup:
        StateMachine<Stage.Code, SmEvent> sm = smFactory.getStateMachine("processId")
        init(Stage.Code.WEB_REQUEST_STAGE, sm)
        when:
        sm.sendEvent(Mono.just(new GenericMessage<SmEvent>(SmEvent.WEB_REQUEST_SUCCEEDED)))
                .doOnComplete(result -> println("Mono completed!"))
                .subscribe()
        then:
        verify(service, times(1)).transit(any())
        sm.getState().getId() == Stage.Code.ASYNC_STAGE
    }

    def "Spring state machine transits from CHECKING to COMPLETED due to WEB_REQUEST_REJECTED event"() {
        setup:
        StateMachine<Stage.Code, SmEvent> sm = smFactory.getStateMachine("processId")
        init(Stage.Code.WEB_REQUEST_STAGE, sm)
        when:
        sm.sendEvent(Mono.just(new GenericMessage<SmEvent>(SmEvent.WEB_REQUEST_REJECTED)))
                .doOnComplete(result -> println("Mono completed!"))
                .subscribe()
        then:
        verify(service, times(1)).complete(any())
        sm.getState().getId() == Stage.Code.COMPLETED
    }

    def "Spring state machine transits from CHECKING to COMPLETED due to WEB_REQUEST_FAILED event"() {
        setup:
        StateMachine<Stage.Code, SmEvent> sm = smFactory.getStateMachine("processId")
        init(Stage.Code.WEB_REQUEST_STAGE, sm)
        when:
        sm.sendEvent(Mono.just(new GenericMessage<SmEvent>(SmEvent.WEB_REQUEST_FAILED)))
                .doOnComplete(result -> println("Mono completed!"))
                .subscribe()
        then:
        verify(service, times(1)).complete(any())
        sm.getState().getId() == Stage.Code.COMPLETED
    }

    def "Spring state machine transits from ASYNC_STAGE to COMPLETED due to ASYNC_SUCCEEDED event"() {
        setup:
        StateMachine<Stage.Code, SmEvent> sm = smFactory.getStateMachine("processId")
        init(Stage.Code.ASYNC_STAGE, sm)
        when:
        sm.sendEvent(Mono.just(new GenericMessage<SmEvent>(SmEvent.ASYNC_SUCCEEDED)))
                .doOnComplete(result -> println("Mono completed!"))
                .subscribe()
        then:
        verify(service, times(1)).transit(any())
        sm.getState().getId() == Stage.Code.COMPLETED
    }

    def "Spring state machine transits from ASYNC_STAGE to COMPLETED due to ASYNC_ERROR event"() {
        setup:
        StateMachine<Stage.Code, SmEvent> sm = smFactory.getStateMachine("processId")
        init(Stage.Code.ASYNC_STAGE, sm)
        when:
        sm.sendEvent(Mono.just(new GenericMessage<SmEvent>(SmEvent.ASYNC_ERROR)))
                .doOnComplete(result -> println("Mono completed!"))
                .subscribe()
        then:
        verify(service, times(1)).complete(any())
        sm.getState().getId() == Stage.Code.COMPLETED
    }
}

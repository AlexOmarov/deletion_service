package ru.somarov.deletion_service.service.state_machine

import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.statemachine.StateContext
import org.springframework.statemachine.config.StateMachineFactory
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import ru.somarov.deletion_service.Application
import ru.somarov.deletion_service.constant.SmEvent
import ru.somarov.deletion_service.constant.SmState
import ru.somarov.deletion_service.service.StateMachineService
import ru.somarov.deletion_service.service.TransitionService
import spock.lang.Specification

import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.*

@ActiveProfiles(profiles = ["test"])
@ContextConfiguration(classes = [Application])
@SpringBootTest
class StateMachineServiceTests extends Specification {

    @Autowired
    @InjectMocks
    StateMachineService service

    @SpyBean
    StateMachineFactory<SmState, SmEvent> factory

    @MockBean
    TransitionService transitionService


    def "Send method sends event to SM and calls transition service"() {
        setup:
        SmState state = SmState.STARTED
        SmEvent event = SmEvent.PROCESS_STARTED
        ArgumentCaptor<StateContext<SmState, SmEvent>> captor = ArgumentCaptor.forClass(StateContext<SmState, SmEvent>.class)
        def id = UUID.randomUUID()
        def sm = spy(factory.getStateMachine(id.toString()))
        reset(factory)
        doReturn(sm).when(factory).getStateMachine(anyString())
        when:
        service.send(service.build(id, state), event)
        then:
        verify(factory, times(1)).getStateMachine(anyString())        || true
        verify(transitionService, times(1)).transit(captor.capture()) || true
        def context = captor.getValue()
        context.getStateMachine().getId() == sm.getId()
        context.getTarget().getId() == SmState.IN_PROGRESS
        context.getEvent() == event
    }
}

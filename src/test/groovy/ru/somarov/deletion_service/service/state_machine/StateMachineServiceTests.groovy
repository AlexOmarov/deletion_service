package ru.somarov.deletion_service.service.state_machine

import ru.somarov.deletion_service.Application
import ru.somarov.deletion_service.conf.TestDataSourceConfiguration
import ru.somarov.deletion_service.constant.state_machine.SmEvent
import ru.somarov.deletion_service.domain.entity.*
import ru.somarov.deletion_service.domain.repository.Dao
import ru.somarov.deletion_service.service.persister.TransitionPersistenceService
import org.apache.camel.test.spring.UseAdviceWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.annotation.Import
import org.springframework.statemachine.ExtendedState
import org.springframework.statemachine.StateContext
import org.springframework.statemachine.config.StateMachineFactory
import org.springframework.statemachine.support.DefaultExtendedState
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.time.LocalDateTime

import static org.mockito.ArgumentMatchers.anyLong
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.*

@Import(TestDataSourceConfiguration)
@ActiveProfiles(profiles = ["test"])
@ContextConfiguration(classes = [Application])
@UseAdviceWith
@SpringBootTest
class StateMachineServiceTests extends Specification {

    @Autowired
    @InjectMocks
    StateMachineService service

    @SpyBean
    StateMachineFactory<Stage.Code, SmEvent> factory

    @MockBean
    TransitionService transitionService

    @MockBean
    TransitionPersistenceService persister

    @MockBean
    Dao dao

    def "start method builds state machine above deletion process and sends first event to it"() {
        setup:
        long id = 1L
        SmEvent event = SmEvent.WEB_REQUEST_STARTED
        ArgumentCaptor<StateContext<Stage.Code, SmEvent>> captor = ArgumentCaptor.forClass(StateContext<Stage.Code, SmEvent>.class)
        def sm = spy(factory.getStateMachine("1"))
        reset(factory)
        doReturn(sm).when(factory).getStateMachine(anyString())
        doReturn(DeletionProcess.builder()
                .id(1L)
                .client(Client.builder().id(1L).build())
                .stage(Stage.builder().code(Stage.Code.STARTED.name()).build())
                .updated(LocalDateTime.now())
                .build()).when(dao).startDeletionProcess(anyLong(), Mockito.any())
        when:
        service.start(id, event, "DEFAULT")
        then:
        verify(factory, times(1)).getStateMachine(anyString())        || true
        verify(transitionService, times(1)).transit(captor.capture()) || true
        def context = captor.getValue()
        context.getStateMachine().getId() == sm.getId()
        context.getTarget().getId() == Stage.Code.WEB_REQUEST_STAGE
        context.getEvent() == event
    }

    def "sendNextEvent sends event to state machine and sends next event in case it is needed"() {
        setup:
        def sm = spy(factory.getStateMachine("1"))
        reset(factory)
        ExtendedState state = spy(new DefaultExtendedState())
        when(state.get("next", SmEvent.class)).thenReturn(SmEvent.WEB_REQUEST_SUCCEEDED, null)
        when(sm.getExtendedState()).thenReturn(state)
        doReturn(sm).when(factory).getStateMachine(anyString())
        def process = DeletionProcess.builder()
                .id(1L)
                .client(Client.builder().id(1L).build())
                .stage(Stage.builder().code(Stage.Code.STARTED.name()).build())
                .updated(LocalDateTime.now())
                .build()
        when(state.get("process", DeletionProcess.class)).thenReturn(process)
        SmEvent event = SmEvent.WEB_REQUEST_STARTED

        ArgumentCaptor<StateContext<Stage.Code, SmEvent>> captor = ArgumentCaptor.forClass(StateContext<Stage.Code, SmEvent>.class)
        when:
        service.sendNextEvent(process, event)
        then:
        verify(factory, times(2)).getStateMachine(anyString())        || true
        verify(transitionService, times(1)).transit(captor.capture()) || true
        def context = captor.getAllValues()
        context[0].getStateMachine().getId() == sm.getId()
        context[0].getTarget().getId() == Stage.Code.ASYNC_STAGE
        context[0].getEvent() == event

        context[1].getStateMachine().getId() == sm.getId()
        context[1].getTarget().getId() == Stage.Code.WEB_REQUEST_STAGE
        context[1].getEvent() == SmEvent.WEB_REQUEST_SUCCEEDED
    }
}

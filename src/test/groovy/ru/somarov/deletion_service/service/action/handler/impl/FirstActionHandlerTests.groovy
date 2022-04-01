package ru.somarov.deletion_service.service.action.handler.impl

import org.mockito.InjectMocks
import ru.somarov.deletion_service.Application
import ru.somarov.deletion_service.conf.TestDataSourceConfiguration
import ru.somarov.deletion_service.constant.SideSystem
import ru.somarov.deletion_service.domain.entity.ActionStatus
import ru.somarov.deletion_service.domain.entity.Client
import ru.somarov.deletion_service.domain.entity.DeletionProcess
import ru.somarov.deletion_service.props.AppProperties
import ru.somarov.deletion_service.service.esb.producer.EsbProducerService
import org.apache.commons.lang3.tuple.Pair
import org.mockito.ArgumentCaptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.*

@Import(TestDataSourceConfiguration)
@ActiveProfiles(profiles = ["test"])
@ContextConfiguration(classes = [Application])
@SpringBootTest
class FirstActionHandlerTests extends Specification {

    @Autowired
    @InjectMocks
    private FirstActionHandler handler

    @MockBean
    private AppProperties appProperties

    @MockBean
    private EsbProducerService esbService

}

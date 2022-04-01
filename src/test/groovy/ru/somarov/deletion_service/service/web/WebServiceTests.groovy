package ru.somarov.deletion_service.service.web

import org.apache.camel.test.spring.UseAdviceWith
import org.mockito.ArgumentCaptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.RequestEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestTemplate
import ru.somarov.deletion_service.Application
import ru.somarov.deletion_service.api.CheckRestrictionsRequest
import ru.somarov.deletion_service.api.CheckRestrictionsResponse
import ru.somarov.deletion_service.conf.TestDataSourceConfiguration
import ru.somarov.deletion_service.domain.entity.Client
import ru.somarov.deletion_service.domain.entity.ClientGroup
import spock.lang.Specification

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.*

@Import(TestDataSourceConfiguration)
@ActiveProfiles(profiles = ["test"])
@ContextConfiguration(classes = [Application])
@UseAdviceWith
@SpringBootTest
class WebServiceTests extends Specification {

    @Autowired
    private WebService service
    @MockBean
    private RestTemplate caller

    def "When getRestrictions is called then DRS is called with valid request and method returns list of restriction codes"() {
        setup:
        Client client = new Client()
        client.setId(1L)
        client.setGroup(new ClientGroup(1L, "DEFAULT"))
        ArgumentCaptor<RequestEntity<CheckRestrictionsRequest>> captor = ArgumentCaptor.forClass(RequestEntity.class)
        CheckRestrictionsResponse response = new CheckRestrictionsResponse()
        if(returnsRestrictions) {
            response.setRestrictions(Arrays.asList("TEST_RESTR","TEST_RESTR2", "TEST_RESTR3"))
        } else {
            response.setRestrictions(new ArrayList<String>())
        }
        doReturn(response).when(caller).exchange(any() as RequestEntity, any() as Class)
        when:
        def result = service.performRequest(client)
        then:
        verify(caller, times(1)).exchange(captor.capture(), any(Class.class)) == null
        def request = captor.getValue()
        request.getBody().getGroup() == "DEFAULT"
        request.getBody().getClientId() == "1"
        result.size() == size
        if (size > 0) {
            result.stream().filter(restriction -> code.equalsIgnoreCase(restriction)).findFirst().orElse(null) != null
        }
        where:
        size | code         | returnsRestrictions
        3    | "TEST_RESTR" | true
        0    | null         | false
    }
}

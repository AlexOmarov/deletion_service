package ru.somarov.deletion_service.props

import ru.somarov.deletion_service.Application
import ru.somarov.deletion_service.conf.TestDataSourceConfiguration
import ru.somarov.deletion_service.constant.SideSystem
import ru.somarov.deletion_service.domain.entity.Action
import ru.somarov.deletion_service.domain.entity.Stage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@Import(TestDataSourceConfiguration)
@ActiveProfiles(profiles = ["test"])
@ContextConfiguration(classes = [Application])
@SpringBootTest(properties = ['app.async.recipients=FIRST_SYSTEM::first-system--Command'])
class AppPropsTests extends Specification {
    @Autowired
    private AppProperties props


    def "Recipients properties are properly parsed and separate maps are made of it"() {
        when:
        def blockRec = props.getRecipients()
        then:
        blockRec.stream().filter(pair -> pair.getKey() == SideSystem.FIRST_SYSTEM
                && pair.getValue().equalsIgnoreCase('first-system--Command')).findFirst().orElse(null) != null
    }
}

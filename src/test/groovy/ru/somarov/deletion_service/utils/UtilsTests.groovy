package ru.somarov.deletion_service.utils

import ru.somarov.deletion_service.constant.SideSystem
import spock.lang.Specification

class UtilsTests extends Specification {
    def "getTopicAndRecipientCode parses incoming string into recipient code and recipient topic"() {
        setup:
        String recipient = "AAA::TOPIC"
        when:
        def pair = Utils.getTopicAndRecipientCode(recipient)
        then:
        pair.getLeft() == SideSystem.AAA
        pair.getRight() == "TOPIC"
    }

    def "getTopicAndRecipientCode doesn't parse string if string doesn't have separator"() {
        setup:
        String recipient = "CODETOPIC"
        when:
        def pair = Utils.getTopicAndRecipientCode(recipient)
        then:
        pair == null
    }

    def "getTopicAndRecipientCode doesn't parse string if string doesn't have one of the parts"() {
        setup:
        String recipient = "CODE::"
        when:
        def pair = Utils.getTopicAndRecipientCode(recipient)
        then:
        pair == null
    }

    // TODO:
    def "fill methods fills passed list of recipients with passed unparsed recipient info"() {

    }

    def "fill methods doesn't add to list recipient properties in invalid format"() {

    }

    def "nextEvent calculates next event if stage is completed"() {

    }
}

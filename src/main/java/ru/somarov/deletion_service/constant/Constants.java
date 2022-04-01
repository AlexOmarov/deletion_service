package ru.somarov.deletion_service.constant;

/**
 * This class is a common constant holder for all application.
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
public final class Constants {

    /* Camel routes uri */
    public static final String IN_ROUTE = "direct:event-consumption";
    public static final String OUT_ROUTE = "direct:push-event-to-kafka";

    /* Esb entity types */
    public static final String ESB_MESSAGE_TYPE_START_PROCESS = "startProcess";
    public static final String ESB_MESSAGE_TYPE_RESULT = "result";

    /* Producer name */
    public static final String PRODUCER_NAME = "DELETION_SERVICE";

    /* Deletion context */
    public static final String CONTEXT = "DEFAULT";

    private Constants() {}
}

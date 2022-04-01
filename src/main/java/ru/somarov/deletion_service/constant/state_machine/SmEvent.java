package ru.somarov.deletion_service.constant.state_machine;

/**
 * Enum which defines events of state machine
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
public enum SmEvent {
    WEB_REQUEST_STARTED, WEB_REQUEST_SUCCEEDED,
    WEB_REQUEST_REJECTED, WEB_REQUEST_FAILED,
    ASYNC_SUCCEEDED, ASYNC_ERROR
}

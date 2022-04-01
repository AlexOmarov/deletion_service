package ru.somarov.deletion_service.service.action.handler;

import ru.somarov.deletion_service.domain.entity.Action;
import ru.somarov.deletion_service.domain.entity.ActionStatus;
import ru.somarov.deletion_service.domain.entity.DeletionProcess;

/**
 * This interface is a contract fo each action handler in application
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 * <p>
 *
 */
public interface ActionHandler {

    /**
     * Function returns action code which should be handled by concrete handler implementation
     *
     * @return Action.Code Code of available action
     * @since 1.0.0
     *
     */
    Action.Code action();

    /**
     * Function returns action code which should be handled by concrete handler implementation
     *
     * @param process Process upon which handler should perform it's logic
     * @return ActionStatus.Code Code of action result
     * @since 1.0.0
     *
     */
    ActionStatus.Code handle(DeletionProcess process);
}

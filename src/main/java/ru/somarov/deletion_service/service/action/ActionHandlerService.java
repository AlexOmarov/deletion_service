package ru.somarov.deletion_service.service.action;

import ru.somarov.deletion_service.domain.entity.Action;
import ru.somarov.deletion_service.domain.entity.ActionStatus;
import ru.somarov.deletion_service.domain.entity.DeletionProcess;
import ru.somarov.deletion_service.service.action.handler.ActionHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * This class is a facade of actions handlers
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 * @see ActionHandler
 *
 */
@Slf4j
@Service
public class ActionHandlerService {

    /**
     * All registered handlers
     */
    private final Map<Action.Code, ActionHandler> handlers = new EnumMap<>(Action.Code.class);

    public ActionHandlerService(List<ActionHandler> handlers) {
        handlers.forEach(handler -> {
            if(handler.action() != null) {
                this.handlers.put(handler.action(), handler);
            } else {
                log.error("There is a action handler which action is null - {}", handler);
            }
        });
    }

    /**
     * Function chooses strategy (handler) based on passed action type and calls chosen handler
     *
     * @param process Deletion process upon which action should be performed
     * @param action  Action code to choose handler
     *
     * @return ActionStatus.Code result of action processing
     * @since 1.0.0
     *
     */
    public ActionStatus.Code handle(@NonNull DeletionProcess process, @NonNull Action.Code action) {
        ActionHandler handler = handlers.get(action);
        if (handler == null) {
            log.error("Cannot get handler for process {} with action {}. Skip performing logic for this action",
                    process.getId(), action);
            return ActionStatus.Code.FAILED;
        }
        return handler.handle(process);
    }

}

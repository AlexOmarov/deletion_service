package ru.somarov.deletion_service.service.action.handler.impl;

import ru.somarov.deletion_service.domain.entity.Action;
import ru.somarov.deletion_service.domain.entity.ActionStatus;
import ru.somarov.deletion_service.domain.entity.DeletionProcess;
import ru.somarov.deletion_service.service.action.handler.ActionHandler;
import ru.somarov.deletion_service.service.persister.ActionPersistenceService;
import ru.somarov.deletion_service.service.web.WebService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Handler for processing web request
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebRequestActionHandler implements ActionHandler {

    private final WebService webService;
    private final ActionPersistenceService persistenceService;

    @Override
    public Action.Code action() {
        return Action.Code.WEB_REQUEST_ACTION;
    }

    @Override
    public ActionStatus.Code handle(@NonNull DeletionProcess process) {
        try {
            List<String> restrictions = webService.performRequest(process.getClient());
            log.debug("Got restrictions for client {}: {}", process.getClient().getId(), Arrays.toString(restrictions.toArray(new String[0])));
            ActionStatus.Code result = restrictions.isEmpty() ? ActionStatus.Code.SUCCEEDED : ActionStatus.Code.REJECTED;
            persistenceService.persistAction(process.getId(), result, action());
            return result;
        } catch (Exception e) {
            log.error("Error while getting restrictions for client {}", process.getClient().getId(), e);
        }
        persistenceService.persistAction(process.getId(), ActionStatus.Code.FAILED, action());
        return ActionStatus.Code.FAILED;
    }
}

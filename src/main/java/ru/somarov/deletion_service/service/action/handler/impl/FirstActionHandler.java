package ru.somarov.deletion_service.service.action.handler.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import ru.somarov.deletion_service.constant.SideSystem;
import ru.somarov.deletion_service.domain.entity.Action;
import ru.somarov.deletion_service.domain.entity.ActionStatus;
import ru.somarov.deletion_service.domain.entity.Client;
import ru.somarov.deletion_service.domain.entity.DeletionProcess;
import ru.somarov.deletion_service.props.AppProperties;
import ru.somarov.deletion_service.service.action.handler.ActionHandler;
import ru.somarov.deletion_service.service.esb.producer.EsbProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handler for processing async action
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FirstActionHandler implements ActionHandler {

    private EsbProducerService service;
    private AppProperties appProperties;

    @Override
    public Action.Code action() {
        return Action.Code.FIRST_ACTION;
    }

    @Override
    public ActionStatus.Code handle(DeletionProcess process) {
        return sendDeletionCommand(SideSystem.FIRST_SYSTEM, process.getClient());
    }

    /**
     * Function chooses strategy (handler) based on passed action type and calls chosen handler
     *
     * @param system System which should receive command
     * @param client Client which info should be passed into outcoming message
     * @return ActionStatus.Code result of command sending
     * @since 1.0.0
     *
     */
    protected ActionStatus.Code sendDeletionCommand(SideSystem system, Client client) {
        Pair<SideSystem, String> recipient = appProperties.getRecipients().stream().filter(pair -> system.equals(pair.getLeft())).findFirst().orElse(null);
        if (recipient == null) {
            log.error("Cannot find recipient for deletion action with system {}. Skipping message for this action", system);
            return ActionStatus.Code.FAILED;
        }
        service.send(client, List.of(recipient));
        return ActionStatus.Code.IN_PROGRESS;
    }
}

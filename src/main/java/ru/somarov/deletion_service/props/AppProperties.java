package ru.somarov.deletion_service.props;

import ru.somarov.deletion_service.constant.SideSystem;
import ru.somarov.deletion_service.domain.entity.Action;
import ru.somarov.deletion_service.domain.entity.Stage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

import static ru.somarov.deletion_service.utils.Utils.fill;

/**
 * This class is responsible for storing configured application properties
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Slf4j
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * Recipients for different esb commands
     */
    private final List<Pair<SideSystem, String>> recipients = new ArrayList<>();

    /**
     * Actions for each process stage that should be performed while transiting to next stage
     */
    Map<Stage.Code, List<Action.Code>> actionsForStages = new EnumMap<>(Stage.Code.class);
    /**
     * Actions for each process stage with link to side system which is responsible for processing action
     */
    Map<Stage.Code, Map<SideSystem, Action.Code>> actionsConfig = new EnumMap<>(Stage.Code.class);

    private Map<String, String> async;

    @PostConstruct
    protected void init() {
        fill(List.of(async.get("recipients").split(",")), this.recipients);

        log.debug("Got following config of block command recipients: {}", Arrays.toString(recipients.toArray()));

        actionsForStages.putAll(Map.of(
                Stage.Code.WEB_REQUEST_STAGE, List.of(Action.Code.WEB_REQUEST_ACTION),
                Stage.Code.ASYNC_STAGE, List.of(Action.Code.FIRST_ACTION),
                Stage.Code.COMPLETED, List.of()
        ));
    }
}


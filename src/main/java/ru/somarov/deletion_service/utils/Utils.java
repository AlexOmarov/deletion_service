package ru.somarov.deletion_service.utils;

import ru.somarov.deletion_service.constant.SideSystem;
import ru.somarov.deletion_service.constant.state_machine.SmEvent;
import ru.somarov.deletion_service.domain.entity.ActionStatus;
import ru.somarov.deletion_service.domain.entity.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple utils class for small tasks
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Slf4j
public final class Utils {

    /**
     * Function parses configuration of esb message recipient
     *
     * @param recipient Unparsed string with recipient code and recipient topic separated by '::'
     * @return Pair<String, String> parsed recipient code and topic
     * @since 1.0.0
     * 2022-01-31
     */
    public static Pair<SideSystem, String> getTopicAndRecipientCode(String recipient) {
        if (!recipient.contains("::")) {
            log.error("Cannot parse configuration of recipient because incoming string is invalid");
            return null;
        }
        String[] split = recipient.split("::");
        if (split.length < 2 || StringUtils.isEmpty(split[0]) || StringUtils.isEmpty(split[1])) {
            log.error("Cannot parse configuration of recipient because incoming string is invalid - {}", recipient);
            return null;
        }
        return Pair.of(SideSystem.valueOf(split[0]), split[1]);
    }

    /**
     * Function fills passed list with configuration of messages recipients
     *
     * @param props      List of unparsed strings with recipient code and recipient topic separated by '::'
     * @param recipients List of parsed properties of recipients
     * @since 1.0.0
     * 2022-02-04
     */
    public static void fill(List<String> props, List<Pair<SideSystem, String>> recipients) {
        props.forEach(recipient -> {
            if (StringUtils.isNotEmpty(recipient) && recipient.contains("::")) {
                recipients.add(getTopicAndRecipientCode(recipient));
            }
        });
    }

    /**
     * Function checks actions codes for stage completion and returns deletion event for transiting to next stage
     *
     * @param stage      Current stage
     * @param results    Results of each action on current stage with count of errors
     * @param errorCount Max attempts for retry
     * @since 1.0.0
     *
     */
    public static SmEvent nextEvent(Stage.Code stage, List<Pair<ActionStatus.Code, Integer>> results, int errorCount) {
        var inProgress = results.stream()
                .filter(result -> result.getLeft().equals(ActionStatus.Code.IN_PROGRESS)).toList();
        var actionsForRetry = results.stream()
                .filter(result -> result.getLeft().equals(ActionStatus.Code.FAILED) && result.getRight() < errorCount).toList();

        if (!inProgress.isEmpty()) {
            log.warn("Cannot get event for next stage due to existence of uncompleted actions: {}. Return null", Arrays.toString(inProgress.toArray()));
            return null;
        }

        if (!actionsForRetry.isEmpty()) {
            log.warn("Cannot get event for next stage due to existence of actions for retry: {}. Return null", Arrays.toString(actionsForRetry.toArray()));
            return null;
        }
        return switch (stage) {
            case WEB_REQUEST_STAGE -> {
                if (results.stream().anyMatch(result -> result.getLeft().equals(ActionStatus.Code.REJECTED))) {
                    yield SmEvent.WEB_REQUEST_REJECTED;
                }
                if (results.stream().anyMatch(result -> result.getLeft().equals(ActionStatus.Code.FAILED))) {
                    yield SmEvent.WEB_REQUEST_FAILED;
                }
                yield SmEvent.WEB_REQUEST_SUCCEEDED;
            }
            case ASYNC_STAGE -> {
                if (results.stream().anyMatch(result -> result.getLeft().equals(ActionStatus.Code.FAILED))) {
                    yield SmEvent.ASYNC_ERROR;
                }
                yield SmEvent.ASYNC_SUCCEEDED;
            }
            default -> null;
        };
    }

    private Utils() {

    }
}

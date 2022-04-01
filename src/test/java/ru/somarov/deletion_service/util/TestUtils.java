package ru.somarov.deletion_service.util;

import ru.somarov.deletion_service.constant.state_machine.SmEvent;
import ru.somarov.deletion_service.domain.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public final class TestUtils {

    private static final AtomicLong aLong = new AtomicLong(10);

    public static long incrementAndGet() {
        return aLong.incrementAndGet();
    }

    private static String convertToInnerFormat(LocalDateTime date) {
        String result = null;
        if(date != null) {
            try{
                result = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            } catch (DateTimeParseException ignore) {

            }
        }
        return result;
    }

    public static DeletionProcess getProcess( long prevStageId, String prevStage) {
        return DeletionProcess.builder()
                .updated(LocalDateTime.now())
                .stage(Stage.builder().id(prevStageId).code(prevStage).build())
                .client(Client.builder().id(1L).build())
                .build();
    }

    public static boolean assertActions(List<ProcessStageAction> actions, List<String> codes) {
        List<Integer> result = new ArrayList<>();
        codes.forEach(code -> {
            ProcessStageAction action = actions.stream().filter(act -> act.getAction().getCode().equals(code) &&
                    act.getActionStatus().getCode().equals(ActionStatus.Code.IN_PROGRESS.name()) &&
                    act.getErrorCount() == 0).findFirst().orElse(null);
            if(action == null) {
                result.add(0);
            }
        });
        return result.isEmpty();
    }

    public static void init(Stage.Code initState, StateMachine<Stage.Code, SmEvent> sm) {
        sm.stopReactively().subscribe();

        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> sma.resetStateMachineReactively(
                        new DefaultStateMachineContext<>(initState, null, null, null, null, "1")
                ).subscribe());

        sm.startReactively().subscribe();
    }
}

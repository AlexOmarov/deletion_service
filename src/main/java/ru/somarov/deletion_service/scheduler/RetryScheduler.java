package ru.somarov.deletion_service.scheduler;

import ru.somarov.deletion_service.scheduler.handler.RetrySchedulerHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * This class is a spring scheduler which is responsible for launching retry mechanism
 * on deletion process actions which has been completed with error
 * but still have some retry attempts left
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.retry.scheduler.enabled", havingValue = "true")
public class RetryScheduler {

    private final RetrySchedulerHandler handler;

    @Scheduled(cron = "${app.action.retry.scheduler.cron}", zone = "CET")
    @SchedulerLock(name = "RetryScheduler")
    public void retry() {
        log.info("Retry scheduler started");
        handler.handle();
        log.info("Retry scheduler finished");
    }
}

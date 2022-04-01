package ru.somarov.deletion_service.scheduler.handler;

import ru.somarov.deletion_service.domain.entity.DeletionProcess;
import ru.somarov.deletion_service.service.retry.RetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This class is a decorator for business service layer
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @see ru.somarov.deletion_service.scheduler.RetryScheduler
 * @see RetryService
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetrySchedulerHandler {

    private final RetryService service;

    @Value("${app.action.retry.batch}")
    private Integer batch;

    /**
     * Function launches retry functionality separated into batches
     *
     * @since 1.0.0
     */
    public void handle() {
        Pageable pageRequest = PageRequest.of(0, batch);
        Pair<Boolean, List<DeletionProcess>> result = service.retry(pageRequest);
        service.checkForCompletion(result.getRight());
        while (Boolean.TRUE.equals(result.getLeft())) {
            // We should not do pageRequest.next() because we are updating actions after retry,
            // and they will not be included in following requests anyway. So if we will pageRequest.next() here,
            // we will just skip another 200 actions, which have risen above after previous retry request
            result = service.retry(pageRequest);
            service.checkForCompletion(result.getRight());
        }
    }
}

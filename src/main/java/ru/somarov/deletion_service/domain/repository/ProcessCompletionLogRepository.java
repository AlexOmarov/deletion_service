package ru.somarov.deletion_service.domain.repository;

import ru.somarov.deletion_service.domain.entity.ProcessCompletionLog;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;

public interface ProcessCompletionLogRepository extends CrudRepository<ProcessCompletionLog, Long> {
    void deleteAllByCreatedLessThan(LocalDateTime datetime);
}

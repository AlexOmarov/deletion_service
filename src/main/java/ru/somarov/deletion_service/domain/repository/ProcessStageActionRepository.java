package ru.somarov.deletion_service.domain.repository;

import ru.somarov.deletion_service.domain.entity.ProcessStageAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;

public interface ProcessStageActionRepository extends CrudRepository<ProcessStageAction, Long> {
    void deleteAllByProcessId(long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    ProcessStageAction findByProcessIdAndActionCode(long id, String code);

    List<ProcessStageAction> findByProcessIdAndErrorCountGreaterThanEqualAndActionStatusCode(long id, int errorCount, String statusCode);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    Page<ProcessStageAction> findByErrorCountLessThanAndActionStatusCodeOrErrorCountLessThanEqualAndActionStatusCodeAndUpdatedLessThanOrderByUpdatedAsc
            (int errorCount, String statusCode, int secondErrorCount, String secondStatusCode, LocalDateTime updated, Pageable pageRequest);

    List<ProcessStageAction> findByProcessId(long id);
}

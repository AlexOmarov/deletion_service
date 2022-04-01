package ru.somarov.deletion_service.domain.repository;

import ru.somarov.deletion_service.domain.entity.DeletionProcess;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;

public interface DeletionProcessRepository extends CrudRepository<DeletionProcess, Long> {
    DeletionProcess findByClientId(long id);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    DeletionProcess findById(long id);
}
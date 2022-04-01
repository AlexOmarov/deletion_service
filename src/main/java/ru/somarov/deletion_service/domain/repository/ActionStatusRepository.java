package ru.somarov.deletion_service.domain.repository;

import ru.somarov.deletion_service.domain.entity.ActionStatus;
import org.springframework.data.repository.CrudRepository;

public interface ActionStatusRepository extends CrudRepository<ActionStatus,Long> {
    ActionStatus findByCode(String code);
}

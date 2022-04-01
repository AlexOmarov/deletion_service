package ru.somarov.deletion_service.domain.repository;

import ru.somarov.deletion_service.domain.entity.Action;
import org.springframework.data.repository.CrudRepository;

public interface ActionRepository extends CrudRepository<Action,Long> {
    Action findByCode(String code);
}



package ru.somarov.deletion_service.domain.repository;

import ru.somarov.deletion_service.domain.entity.ClientGroup;
import org.springframework.data.repository.CrudRepository;

public interface ClientGroupRepository extends CrudRepository <ClientGroup, Long> {
    ClientGroup findByCode(String code);
}

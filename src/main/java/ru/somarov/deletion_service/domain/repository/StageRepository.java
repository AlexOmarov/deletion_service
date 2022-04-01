package ru.somarov.deletion_service.domain.repository;

import ru.somarov.deletion_service.domain.entity.Stage;
import org.springframework.data.repository.CrudRepository;

public interface StageRepository extends CrudRepository <Stage, Long> {
    Stage findByCode(String code);
}

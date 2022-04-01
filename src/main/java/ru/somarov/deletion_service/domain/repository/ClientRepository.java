package ru.somarov.deletion_service.domain.repository;

import ru.somarov.deletion_service.domain.entity.Client;
import org.springframework.data.repository.CrudRepository;

public interface ClientRepository extends CrudRepository<Client,Long> { }

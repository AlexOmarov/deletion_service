package ru.somarov.deletion_service.domain.entity;

import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

/**
 * This class is a database entity, represents client groups.
 * Client group defines set of available deletion processes, required actions and
 * other rules of deletion
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "client_group")
// Hibernate second level cache is disabled by spring.jpa.properties.hibernate.cache.use_second_level_cache=false
@Cacheable
@Cache(region = "client_group", usage = CacheConcurrencyStrategy.READ_ONLY)
public class ClientGroup {
    @Id
    private Long id;
    private String code;

    public enum Code { DEFAULT }
}

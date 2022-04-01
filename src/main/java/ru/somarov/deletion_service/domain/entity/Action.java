package ru.somarov.deletion_service.domain.entity;

import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This class is a database entity, represents action types which can be performed during
 * deletion process execution
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "action")
// Hibernate second level cache is disabled by spring.jpa.properties.hibernate.cache.use_second_level_cache=false
@Cacheable
@Cache(region = "action", usage = CacheConcurrencyStrategy.READ_ONLY)
public class Action {
    @Id
    private Long id;
    /**
     * Action code
     */
    private String code;
    /**
     * Simple description for each action
     */
    private String description;

    public enum Code { WEB_REQUEST_ACTION, FIRST_ACTION }
}

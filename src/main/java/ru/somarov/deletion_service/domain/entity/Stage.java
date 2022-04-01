package ru.somarov.deletion_service.domain.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This class is a database entity, represents deletion process stage.
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
@Table(name = "stage")
public class Stage {
    @Id
    private Long id;
    private String code;
    private String description;

    public enum Code { STARTED, WEB_REQUEST_STAGE, ASYNC_STAGE, COMPLETED }
}

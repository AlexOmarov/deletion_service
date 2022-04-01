package ru.somarov.deletion_service.domain.entity;

import ru.somarov.deletion_service.domain.entity.jsonb.Completion;
import ru.somarov.deletion_service.domain.entity.jsonb.types.CompletionPgJsonPropertyType;
import lombok.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * This class is a database entity, represents log of deletion process completion
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
@Immutable
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "process_completion_log")
@TypeDef(name = "jsonbCompletion", typeClass = CompletionPgJsonPropertyType.class)
public class ProcessCompletionLog {
    @Id
    private UUID id;

    @Type(type = "jsonbCompletion")
    @Column(columnDefinition = "jsonb")
    private Completion completion;

    private LocalDateTime created;
}

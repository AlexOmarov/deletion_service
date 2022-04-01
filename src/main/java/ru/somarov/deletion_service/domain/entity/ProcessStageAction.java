package ru.somarov.deletion_service.domain.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * This class is a database entity, represents current actions of every process.
 * Concrete set of such actions is refreshed for each new stage of process and
 * depends on stage, process type and client group.
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
@Table(name = "process_stage_action")
public class ProcessStageAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Last time of every update
     */
    private LocalDateTime updated;
    /**
     * Count of gotten errors while trying to perform or redo this action
     */
    private Integer errorCount;
    /**
     * Description of last gotten error
     */
    private String errorDescription;

    @ManyToOne
    @JoinColumn(name = "deletion_process_id")
    private DeletionProcess process;

    @ManyToOne
    @JoinColumn(name = "action_id")
    private Action action;
    @ManyToOne
    @JoinColumn(name = "action_status_id")
    private ActionStatus actionStatus;
}

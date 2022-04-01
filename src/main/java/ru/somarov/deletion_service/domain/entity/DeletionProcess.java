package ru.somarov.deletion_service.domain.entity;


import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * This class is a database entity, represents deletion process.
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
@Table(name = "deletion_process")
public class DeletionProcess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Last time of every update
     */
    private LocalDateTime updated;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "stage_id")
    private Stage stage;
}

package ru.somarov.deletion_service.domain.entity;

import lombok.*;

import javax.persistence.*;

/**
 * This class is a database entity, represents clients which are in process of deletion
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
@Table(name = "client")
public class Client {
    @Id
    private Long id;
    @ManyToOne
    @JoinColumn(name = "client_group_id")
    private ClientGroup group;
}

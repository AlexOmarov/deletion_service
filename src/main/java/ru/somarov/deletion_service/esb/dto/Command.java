package ru.somarov.deletion_service.esb.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Command extends EsbMessage {
    private String actionId;
}

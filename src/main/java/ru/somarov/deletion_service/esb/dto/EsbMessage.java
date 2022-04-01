package ru.somarov.deletion_service.esb.dto;

import lombok.Data;

@Data
public class EsbMessage {
    private String clientId;
    private String group;
    private String status;
}

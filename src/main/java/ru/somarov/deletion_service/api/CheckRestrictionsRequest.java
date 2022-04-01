package ru.somarov.deletion_service.api;

import lombok.Data;

import java.io.Serializable;

@Data
public class CheckRestrictionsRequest implements Serializable {
    private String group;
    private String clientId;
    private String context;
}

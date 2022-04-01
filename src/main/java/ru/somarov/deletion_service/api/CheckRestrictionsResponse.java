package ru.somarov.deletion_service.api;

import lombok.Data;

import java.util.List;

@Data
public class CheckRestrictionsResponse {
    private List<String> restrictions;
    private String clientId;
}

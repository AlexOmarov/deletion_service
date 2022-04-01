package ru.somarov.deletion_service.service.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.somarov.deletion_service.api.CheckRestrictionsRequest;
import ru.somarov.deletion_service.api.CheckRestrictionsResponse;
import ru.somarov.deletion_service.domain.entity.Client;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is a service for all outcoming web calls across application
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebService {

    private final RestTemplate caller;

    @Value("${app.web.host}")
    private String drsHost;
    @Value("${app.web.endpoint}")
    private String endpoint;

    /**
     * Function performs web request
     *
     * @param client Client for which request should be made
     *
     * @return List<String> list
     * @since 1.0.0
    */
    public List<String> performRequest(Client client)  {
        log.debug("Got restriction request for client {}", client.getId());
        var result = new ArrayList<String>();

        var request = new CheckRestrictionsRequest();
        request.setGroup(client.getGroup().getCode());
        request.setClientId(client.getId().toString());
        RequestEntity<CheckRestrictionsRequest> entity = new RequestEntity<>(request, HttpMethod.POST, URI.create(drsHost + endpoint));

        ResponseEntity<CheckRestrictionsResponse> response;

        response = caller.exchange(entity, CheckRestrictionsResponse.class);

        if(!response.getStatusCode().is2xxSuccessful()) {
            log.error("Cannot check restrictions for client {} - result code: {}", client.getId(), response.getStatusCode());
            return null;
        }
        CheckRestrictionsResponse body = response.getBody();
        if(body != null && !body.getRestrictions().isEmpty()) {
            log.warn("Client {} has restrictions {}", body.getClientId(), Arrays.toString(body.getRestrictions().toArray(new String[0])));
            result.addAll(body.getRestrictions());
        }
        return result;
    }
}

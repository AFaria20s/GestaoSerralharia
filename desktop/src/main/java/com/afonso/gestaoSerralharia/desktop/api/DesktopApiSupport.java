package com.afonso.gestaoSerralharia.desktop.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class DesktopApiSupport {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public DesktopApiSupport(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public <T> T get(String path, Class<T> bodyType) {
        return execute(() -> restClient.get().uri(path).retrieve().body(bodyType));
    }

    public <T> T get(String path, ParameterizedTypeReference<T> bodyType) {
        return execute(() -> restClient.get().uri(path).retrieve().body(bodyType));
    }

    public <T> T post(String path, Object body, Class<T> bodyType) {
        return exchange(HttpMethod.POST, path, body, bodyType);
    }

    public <T> T put(String path, Object body, Class<T> bodyType) {
        return exchange(HttpMethod.PUT, path, body, bodyType);
    }

    public void delete(String path) {
        execute(() -> {
            restClient.delete().uri(path).retrieve().toBodilessEntity();
            return null;
        });
    }

    public String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private <T> T exchange(HttpMethod method, String path, Object body, Class<T> bodyType) {
        return execute(() -> {
            RestClient.RequestBodySpec spec = restClient.method(method)
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON);
            if (body == null) {
                return spec.retrieve().body(bodyType);
            }
            return spec.body(body).retrieve().body(bodyType);
        });
    }

    private <T> T execute(ApiCall<T> call) {
        try {
            return call.run();
        } catch (RestClientResponseException ex) {
            throw new ApiClientException(extractMessage(ex), ex);
        } catch (ResourceAccessException ex) {
            throw new ApiClientException("Não foi possível contactar a API. Confirma que o backend está a correr.", ex);
        }
    }

    private String extractMessage(RestClientResponseException ex) {
        String body = ex.getResponseBodyAsString();
        if (body == null || body.isBlank()) {
            return "Erro ao comunicar com a API (" + ex.getRawStatusCode() + ")";
        }

        try {
            Map<String, Object> payload = objectMapper.readValue(body, new TypeReference<>() {});
            Object erro = payload.get("erro");
            if (erro != null) {
                return erro.toString();
            }
        } catch (Exception ignored) {
        }

        return body;
    }

    @FunctionalInterface
    private interface ApiCall<T> {
        T run();
    }
}

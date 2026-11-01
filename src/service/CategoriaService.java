package service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.AppConfig;
import model.CategoriaDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class CategoriaService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CategoriaService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public List<CategoriaDTO> listarCategorias(String tokenJwt) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.getApiUrl() + "/categorias"))
                    .GET();

            if (tokenJwt != null && !tokenJwt.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + tokenJwt);
            }

            HttpResponse<String> response = httpClient.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && response.body() != null) {
                String jsonBody = response.body();
                return objectMapper.readValue(jsonBody, new TypeReference<List<CategoriaDTO>>() {
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void salvarCategoria(String nomeCategoria, String tokenJwt) throws Exception {
        var payloadMap = java.util.Map.of("nome", nomeCategoria);
        String jsonPayload = objectMapper.writeValueAsString(payloadMap);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiUrl() + "/categorias"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload));
        requestBuilder.header("Authorization", "Bearer " + tokenJwt);
        HttpResponse<String> response = httpClient.send(requestBuilder.build(),
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new RuntimeException("Erro ao cadastrar categoria. Código: "
                    + response.statusCode()
                    + " - " + response.body());
        }
    }
}
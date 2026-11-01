package service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.AppConfig;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;

public class AuthService {
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String realizarLogin(String username, String password) throws IOException {
        String jsonPayload = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "password", password
        ));
        RequestBody body = RequestBody.create(jsonPayload,
                MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(AppConfig.getApiUrl() + "/login")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Usuário ou senha inválidos! Código: " + response.code());
            }

            String responseBody = response.body().string();

            Map<String, String> resultado = objectMapper.readValue(responseBody,
                    new TypeReference<>() {
            });

            return resultado.get("token");
        }
    }
}
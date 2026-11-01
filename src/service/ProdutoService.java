package service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.AppConfig;
import model.ProdutoDTO;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProdutoService {

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public ProdutoService() {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public String salvarProduto(ProdutoDTO dto, File foto, File manual, String tokenJwt) throws IOException {
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        String jsonProduto = objectMapper.writeValueAsString(dto);
        bodyBuilder.addFormDataPart("produto", "produto.json",
                RequestBody.create(jsonProduto, MediaType.parse("application/json; charset=utf-8")));
        if (foto != null && foto.exists()) {
            bodyBuilder.addFormDataPart("foto", foto.getName(),
                    RequestBody.create(foto, MediaType.parse("image/*")));
        }
        if (manual != null && manual.exists()) {
            bodyBuilder.addFormDataPart("manual", manual.getName(),
                    RequestBody.create(manual, MediaType.parse("application/pdf")));
        }
        RequestBody requestBody = bodyBuilder.build();

        Request.Builder requestBuilder = new Request.Builder();
        String retorno = "Produto cadastrado com sucesso";
        if (dto.getId() != null) {
            requestBuilder.url(AppConfig.getApiUrl() + "/produtos/" + dto.getId()).put(requestBody);
            retorno = "Produto alterado com sucesso";
        } else {
            requestBuilder.url(AppConfig.getApiUrl() + "/produtos").post(requestBody);
        }
        requestBuilder.header("Authorization", "Bearer " + tokenJwt);

        try (Response response = client.newCall(requestBuilder.build()).execute()) {
            if (!response.isSuccessful()) {
                String corpoErro = response.body() != null ? response.body().string() : "Sem corpo de resposta";
                System.err.println("Código: " + response.code());
                System.err.println("Mensagem: " + response.message());
                System.err.println("Corpo do erro retornado pela API: " + corpoErro);
                throw new RuntimeException("Erro ao acessar API. Código: " + response.code());
            }
            return retorno;
        }
    }

    public List<ProdutoDTO> listarProdutos(String tokenJwt) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(AppConfig.getApiUrl() + "/produtos")
                .get();
        requestBuilder.header("Authorization", "Bearer " + tokenJwt);

        try (Response response = client.newCall(requestBuilder.build()).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                return objectMapper.readValue(jsonResponse, new TypeReference<>() {
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void excluirProduto(Long id, String tokenJwt) throws IOException {
        String urlExclusao = AppConfig.getApiUrl() + "/produtos/" + id;

        Request.Builder requestBuilder = new Request.Builder()
                .url(urlExclusao)
                .delete();

        requestBuilder.header("Authorization", "Bearer " + tokenJwt);

        try (Response response = client.newCall(requestBuilder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Erro ao excluir produto. Código: " + response.code());
            }
        }
    }
}
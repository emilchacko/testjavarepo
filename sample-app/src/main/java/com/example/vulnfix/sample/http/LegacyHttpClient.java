package com.example.vulnfix.sample.http;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LegacyHttpClient {
    public String get(String url) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            return client.execute(new HttpGet(url), response -> {
                int statusCode = response.getCode();
                if (statusCode < 200 || statusCode >= 300) {
                    throw new IOException("HTTP GET failed with status " + statusCode);
                }
                return response.getEntity() == null ? "" : EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            });
        }
    }
}
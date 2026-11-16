package com.zamcan.codelab;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Network-only certificate verification. Never trusts client-side eligibility or scores. */
public final class CertificationClient {
    private CertificationClient() {}
    public interface Callback { void completed(int httpStatus, String response, Exception error); }
    public static void verify(String baseUrl, String certificateId, Callback callback) {
        if (callback == null) throw new IllegalArgumentException("callback required");
        if (baseUrl == null || !baseUrl.matches("https://[A-Za-z0-9.-]+")) {
            callback.completed(0, null, new IllegalArgumentException("Verified HTTPS API base URL required")); return;
        }
        if (certificateId == null || !certificateId.matches("[A-Za-z0-9_-]{1,100}")) {
            callback.completed(0, null, new IllegalArgumentException("Invalid certificate ID")); return;
        }
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(baseUrl + "/v1/credentials/" + certificateId + "/status");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setRequestProperty("Accept", "application/json");
                int status = connection.getResponseCode();
                java.io.InputStream stream = status < 400 ? connection.getInputStream() : connection.getErrorStream();
                StringBuilder result = new StringBuilder();
                if (stream != null) try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    String line; while ((line = reader.readLine()) != null && result.length() < 65536) result.append(line);
                }
                callback.completed(status, result.toString(), null);
            } catch (Exception error) { callback.completed(0, null, error); }
            finally { if (connection != null) connection.disconnect(); }
        }, "codelab-certificate-verify").start();
    }
}

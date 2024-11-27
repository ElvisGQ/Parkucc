package com.example.parkucc;

import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;

public class OkHttpHelper {

    private static final String AUTH_TOKEN = "irving_garnachas"; // Replace this with your token
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client;

    public OkHttpHelper() {
        client = new OkHttpClient();
    }

    /**
     * General method to execute requests
     */
    private void executeRequest(Request request, Callback callback) {
        client.newCall(request).enqueue(callback);
    }

    /**
     * GET request
     */
    public void get(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + AUTH_TOKEN)
                .get()
                .build();

        executeRequest(request, callback);
    }

    /**
     * POST request
     */
    public void post(String url, JSONObject json, Callback callback) {
        RequestBody body = RequestBody.create(json.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + AUTH_TOKEN)
                .post(body)
                .build();

        executeRequest(request, callback);
    }

    /**
     * PUT request
     */
    public void put(String url, JSONObject json, Callback callback) {
        RequestBody body = RequestBody.create(json.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + AUTH_TOKEN)
                .put(body)
                .build();

        executeRequest(request, callback);
    }

    /**
     * DELETE request
     */
    public void delete(String url, JSONObject json, Callback callback) {
        RequestBody body = RequestBody.create(json.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + AUTH_TOKEN)
                .delete(body)
                .build();

        executeRequest(request, callback);
    }
}

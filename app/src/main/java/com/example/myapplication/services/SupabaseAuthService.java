package com.example.myapplication.services;

import com.example.myapplication.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SupabaseAuthService {

    private final OkHttpClient client;
    private final String baseUrl = BuildConfig.SUPABASE_URL;
    private final String apiKey = BuildConfig.SUPABASE_ANON_KEY;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public SupabaseAuthService() {
        this.client = new OkHttpClient();
    }

    /**
     * Registers a new user via Supabase GoTrue API
     */
    public void signUp(String email, String password, String name, Callback callback) {
        String url = baseUrl + "/auth/v1/signup";
        JSONObject jsonBody = new JSONObject();

        try {
            JSONObject dataBody = new JSONObject();
            dataBody.put("full_name", name);
            jsonBody.put("email", email);
            jsonBody.put("password", password);
            jsonBody.put("data", dataBody);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    /**
     * Logs in an existing user and returns JWT tokens
     */
    public void login(String email, String password, Callback callback) {
        String url = baseUrl + "/auth/v1/token?grant_type=password";
        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }
}
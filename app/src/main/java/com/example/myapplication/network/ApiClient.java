package com.example.myapplication.network;

import android.content.Context;
import okhttp3.OkHttpClient;

public class ApiClient {

    private static OkHttpClient client;

    // Returns a globally accessible, authenticated OkHttpClient
    public static OkHttpClient getInstance(Context context) {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    // Attach your custom interceptor here
                    .addInterceptor(new AuthInterceptor(context.getApplicationContext()))
                    .build();
        }
        return client;
    }
}
package com.example.myapplication.network;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.example.myapplication.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // 1. Start building a new request based on the original one
        // Pulling the anon key from your environment variables
        String anonKey = BuildConfig.SUPABASE_ANON_KEY;
        Request.Builder requestBuilder = originalRequest.newBuilder()
                .header("apikey", anonKey) // Supabase always requires this
                .header("Content-Type", "application/json");

        // 2. Fetch the current session token
        SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);

        // 3. If a token exists (user is logged in), attach it as a Bearer token
        if (accessToken != null && !accessToken.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + accessToken);
        }

        // 4. Build the final request and let it proceed to the server
        Request newRequest = requestBuilder.build();
        return chain.proceed(newRequest);
    }
}
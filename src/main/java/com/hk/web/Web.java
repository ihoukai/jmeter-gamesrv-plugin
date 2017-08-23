package com.hk.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Web {
    private static Web instance;
    private static Retrofit retrofit;
    private volatile boolean isInit;

    public static synchronized Web getInstance() {
        if (instance == null) {
            instance = new Web();
        }
        return instance;
    }

    private Web() {
    }

    public synchronized Web init(String baseUrl, boolean isLogEnable) {
        if (isInit) {
            return this;
        }
        isInit = true;
        OkHttpClient httpClient;
        if (isLogEnable) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient = new OkHttpClient.Builder().addInterceptor(logging).build();
        } else {
            httpClient = new OkHttpClient.Builder().build();
        }

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd hh:mm:ss")
                .create();
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return this;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}

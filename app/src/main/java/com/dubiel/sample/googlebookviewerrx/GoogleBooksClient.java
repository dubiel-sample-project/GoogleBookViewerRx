package com.dubiel.sample.googlebookviewerrx;


import android.support.annotation.NonNull;

import com.dubiel.sample.googlebookviewerrx.data.BookListItems;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;

public class GoogleBooksClient {
    private static final String GOOGLE_BOOKS_URL = "https://www.googleapis.com/";

    private static GoogleBooksClient instance;
    private GoogleBooksService googleBooksService;

    private GoogleBooksClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        final Gson gson =
                new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GOOGLE_BOOKS_URL)
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        googleBooksService = retrofit.create(GoogleBooksService.class);
    }

    public static GoogleBooksClient getInstance() {
        if (instance == null) {
            instance = new GoogleBooksClient();
        }
        return instance;
    }

    public Observable<BookListItems> getBooks(@NonNull SearchData data) {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("key", data.getKey());
        queryMap.put("q", data.getQuery());
        queryMap.put("fields", "items(id,selfLink,volumeInfo/title,volumeInfo/imageLinks/smallThumbnail)");
        queryMap.put("startIndex", data.getStart().toString());
        queryMap.put("maxResults", data.getMaxResults().toString());

        return googleBooksService.queryBooks(queryMap);
    }

    public Observable<BookListItems> getBooks(@NonNull String key, @NonNull String query, @NonNull Integer startIndex, @NonNull Integer maxResults) {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("key", key);
        queryMap.put("q", query);
        queryMap.put("fields", "items(id,selfLink,volumeInfo/title,volumeInfo/imageLinks/smallThumbnail)");
        queryMap.put("startIndex", startIndex.toString());
        queryMap.put("maxResults", maxResults.toString());

        return googleBooksService.queryBooks(queryMap);
    }
}

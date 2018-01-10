package com.dubiel.sample.googlebookviewerrx;


import com.dubiel.sample.googlebookviewerrx.data.BookDetailItem;
import com.dubiel.sample.googlebookviewerrx.data.BookListItems;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Single;

public interface GoogleBooksService {
    @GET("books/v1/volumes")
    Observable<BookListItems> queryBooks(@QueryMap Map<String, String> query);

    @GET("books/v1/volumes/{volumeId}")
    Single<BookDetailItem> queryVolume(@Path("volumeId") String volumeId);
}

package com.dubiel.sample.googlebookviewerrx;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.dubiel.sample.googlebookviewerrx.data.BookListItems;

import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        subscription = GoogleBooksClient.getInstance()
                .getBooks("AIzaSyBaTPJ5YXt5V6VSuvnxhIgj4NJZ1vJqtmM", "cats")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BookListItems>() {
                    @Override public void onCompleted() {
                        System.out.println("In onCompleted()");
                    }

                    @Override public void onError(Throwable e) {
                        e.printStackTrace();
                        System.out.println("In onError()");
                        System.out.println(e.getCause());
                    }

                    @Override public void onNext(BookListItems books) {
                        System.out.println("In onNext()");
                        System.out.println("BookListItems length: " + books.getItems().length);
                    }
                });
    }
}

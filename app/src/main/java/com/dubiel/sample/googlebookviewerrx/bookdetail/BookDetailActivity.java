package com.dubiel.sample.googlebookviewerrx.bookdetail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.dubiel.sample.googlebookviewerrx.R;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasFragmentInjector;
import dagger.android.support.DaggerAppCompatActivity;
import dagger.android.support.HasSupportFragmentInjector;

public class BookDetailActivity extends DaggerAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putString(BookDetailActivityFragment.ARG_VOLUME_ID,
                    getIntent().getStringExtra(BookDetailActivityFragment.ARG_VOLUME_ID));
            BookDetailActivityFragment fragment = new BookDetailActivityFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.book_item_detail_container, fragment)
                    .commit();
        }
    }

}

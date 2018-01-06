package com.dubiel.sample.googlebookviewerrx.bookdetail;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dubiel.sample.googlebookviewerrx.R;

public class BookDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putString(BookDetailActivityFragment.ARG_SELF_LINK,
                    getIntent().getStringExtra(BookDetailActivityFragment.ARG_SELF_LINK));
            BookDetailActivityFragment fragment = new BookDetailActivityFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.book_item_detail_container, fragment)
                    .commit();
        }
    }

}

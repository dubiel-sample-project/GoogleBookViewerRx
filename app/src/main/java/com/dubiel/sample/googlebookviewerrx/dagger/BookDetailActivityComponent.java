package com.dubiel.sample.googlebookviewerrx.dagger;


import com.dubiel.sample.googlebookviewerrx.MainActivity;
import com.dubiel.sample.googlebookviewerrx.bookdetail.BookDetailActivity;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent(modules = BookDetailActivityModule.class)
public interface BookDetailActivityComponent extends AndroidInjector<BookDetailActivity> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<BookDetailActivity> {
    }
}

package com.dubiel.sample.googlebookviewerrx.dagger;


import com.dubiel.sample.googlebookviewerrx.bookdetail.BookDetailActivityFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class BookDetailFragmentProvider {

    @ContributesAndroidInjector(modules = BookDetailActivityFragmentModule.class)
    abstract BookDetailActivityFragment provideBookDetailActivityFragmentFactory();
}

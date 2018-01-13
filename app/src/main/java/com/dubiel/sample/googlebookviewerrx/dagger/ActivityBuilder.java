package com.dubiel.sample.googlebookviewerrx.dagger;


import android.app.Activity;

import com.dubiel.sample.googlebookviewerrx.MainActivity;
import com.dubiel.sample.googlebookviewerrx.bookdetail.BookDetailActivity;
import com.dubiel.sample.googlebookviewerrx.bookdetail.BookDetailActivityFragment;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoMap;

@Module
public abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = MainActivityModule.class)
    abstract MainActivity bindMainActivity();

    @ContributesAndroidInjector(modules = {BookDetailActivityModule.class, BookDetailFragmentProvider.class})
    abstract BookDetailActivity bindBookDetailActivity();
}
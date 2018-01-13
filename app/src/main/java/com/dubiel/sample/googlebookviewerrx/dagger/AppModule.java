package com.dubiel.sample.googlebookviewerrx.dagger;


import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.dubiel.sample.googlebookviewerrx.MainActivity;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoMap;

@Module
public abstract class AppModule {
    @Binds
    abstract Context provideContext(Application application);
}

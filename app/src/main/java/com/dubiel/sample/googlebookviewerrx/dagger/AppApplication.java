package com.dubiel.sample.googlebookviewerrx.dagger;


import android.app.Activity;
import android.app.Application;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import dagger.android.HasActivityInjector;
import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;

public class AppApplication extends DaggerApplication {
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        AppComponent appComponent = DaggerAppComponent.builder().application(this).build();
        appComponent.inject(this);
        return appComponent;
    }
}

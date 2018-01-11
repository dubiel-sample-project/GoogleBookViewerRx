package com.dubiel.sample.googlebookviewerrx.dagger;


import android.app.Activity;
import android.app.Application;

import dagger.android.HasActivityInjector;
import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;

public class AppApplication extends Application implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

    @Override
    public void onCreate() {
        super.onCreate();
        DaggerAppApplicationComponent.create().inject(this);
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }
}

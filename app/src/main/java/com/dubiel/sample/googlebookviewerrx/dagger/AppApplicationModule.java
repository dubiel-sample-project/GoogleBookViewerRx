package com.dubiel.sample.googlebookviewerrx.dagger;


import com.dubiel.sample.googlebookviewerrx.MainActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class AppApplicationModule {
    @ContributesAndroidInjector
    abstract MainActivity contributeActivityInjector();
}

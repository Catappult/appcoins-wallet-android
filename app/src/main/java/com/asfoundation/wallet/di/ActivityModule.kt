package com.asfoundation.wallet.di

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
class ActivityModule {

  @Provides
  fun provideFragmentManager(activity: Activity): FragmentManager =
    (activity as AppCompatActivity).supportFragmentManager

}
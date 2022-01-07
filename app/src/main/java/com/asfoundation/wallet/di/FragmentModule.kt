package com.asfoundation.wallet.di

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class FragmentModule {

  @Provides
  fun provideFragmentManager(activity: Activity): FragmentManager =
    (activity as AppCompatActivity).supportFragmentManager

  @Provides
  fun provideNavController(fragment: Fragment): NavController {
    return fragment.findNavController()
  }
}

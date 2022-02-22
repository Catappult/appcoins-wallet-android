package com.asfoundation.wallet.di

import androidx.fragment.app.Fragment
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
  fun provideNavController(fragment: Fragment): NavController {
    return fragment.findNavController()
  }
}
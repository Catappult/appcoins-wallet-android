package com.asfoundation.wallet.iab.di

import androidx.navigation.NavController
import com.asfoundation.wallet.iab.FragmentNavigator
import dagger.assisted.AssistedFactory

@AssistedFactory
interface FragmentNavigatorFactory {

  fun create(navController: NavController): FragmentNavigator
}

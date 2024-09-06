package com.asfoundation.wallet.iab.presentation.main

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.asfoundation.wallet.iab.FragmentNavigator

class MainViewModel(
  private val fragmentNavigator: FragmentNavigator
) : ViewModel() {

  fun navigateTo(directions: NavDirections) {
    fragmentNavigator.navigateTo(directions)
  }
}

@Composable
fun rememberMainViewModel(
  navController: NavController,
): MainViewModel {
  return viewModel<MainViewModel>(
    key = navController.hashCode().toString(),
    factory = object : Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(
          fragmentNavigator = FragmentNavigator(navController),
        ) as T
      }
    }
  )
}

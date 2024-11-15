package com.asfoundation.wallet.iab

import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class FragmentNavigator @AssistedInject constructor(
  @Assisted private val navController: NavController,
  private val displayChatUseCase: DisplayChatUseCase,
) {

  fun navigateTo(directions: NavDirections, navOptions: NavOptions? = null) {
    navController.navigate(directions, navOptions)
  }

  fun navigateTo(destiny: Int, args: Bundle? = null, navOptions: NavOptions? = null) {
    navController.navigate(destiny, args, navOptions)
  }

  fun popBackStack() {
    navController.popBackStack()
  }

  fun startActivity(newIntent: Intent) {
    navController.context.startActivity(newIntent)
  }

  fun onSupportClick() {
    displayChatUseCase()
  }

  fun navigateUp() {
    navController.navigateUp()
  }

}

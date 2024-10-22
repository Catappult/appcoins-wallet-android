package com.asfoundation.wallet.iab

import android.content.Intent
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class FragmentNavigator @AssistedInject constructor(
  @Assisted private val navController: NavController,
  private val displayChatUseCase: DisplayChatUseCase,
) {

  fun navigateTo(directions: NavDirections) {
    navController.navigate(directions)
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

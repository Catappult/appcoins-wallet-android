package com.asfoundation.wallet.iab

import android.content.Intent
import androidx.navigation.NavController
import androidx.navigation.NavDirections

class FragmentNavigator(
  private val navController: NavController
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

}

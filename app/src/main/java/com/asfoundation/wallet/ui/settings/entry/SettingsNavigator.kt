package com.asfoundation.wallet.ui.settings.entry

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.asf.wallet.R
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import javax.inject.Inject

class SettingsNavigator @Inject constructor(private val fragment: Fragment) {

  fun showAuthentication(
    authenticationResultLauncher: ActivityResultLauncher<Intent>
  ) {
    val intent = AuthenticationPromptActivity.newIntent(fragment.requireContext())
      .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    authenticationResultLauncher.launch(intent)
  }

  fun navigateToManageWallet(navController: NavController) {
    navController.navigate(R.id.action_navigate_to_manage_wallet)
  }

  fun navigateToChangeCurrency(navController: NavController) {
    navController.navigate(R.id.action_navigate_to_change_fiat_currency)
  }

  fun navigateToPersonalInformation(navController: NavController) {
    navController.navigate(R.id.action_navigate_to_personal_information)
  }
}

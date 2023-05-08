package com.asfoundation.wallet.wallet_reward


import android.content.Intent
import androidx.fragment.app.Fragment
import com.appcoins.wallet.ui.arch.data.Navigator
import com.asfoundation.wallet.ui.settings.SettingsActivity
import javax.inject.Inject

class RewardNavigator @Inject constructor(
  private val fragment: Fragment
) : Navigator {

  fun navigateToSettings(turnOnFingerprint: Boolean = false) {
    val intent = SettingsActivity.newIntent(fragment.requireContext(), turnOnFingerprint)
    openIntent(intent)
  }
  fun openIntent(intent: Intent) = fragment.requireContext()
    .startActivity(intent)
}

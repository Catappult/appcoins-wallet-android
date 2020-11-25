package com.asfoundation.wallet.ui.settings.entry

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import com.asfoundation.wallet.ui.backup.BackupActivity
import com.asfoundation.wallet.ui.settings.wallets.SettingsWalletsFragment
import com.asfoundation.wallet.ui.wallets.WalletsModel

class SettingsNavigator(private val fragmentManager: FragmentManager,
                        private val activity: FragmentActivity) {

  companion object {
    private const val AUTHENTICATION_REQUEST_CODE = 33
  }

  fun showAuthentication() {
    val intent = AuthenticationPromptActivity.newIntent(activity)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    activity.startActivityForResult(intent, AUTHENTICATION_REQUEST_CODE)
  }

  fun navigateToBackup(walletAddress: String) {
    activity.startActivity(BackupActivity.newIntent(activity, walletAddress))
  }

  fun showWalletsBottomSheet(walletModel: WalletsModel) {
    fragmentManager.beginTransaction()
        .setCustomAnimations(R.anim.fade_in_animation, R.anim.fragment_slide_down,
            R.anim.fade_in_animation, R.anim.fragment_slide_down)
        .replace(R.id.bottom_sheet_fragment_container,
            SettingsWalletsFragment.newInstance(walletModel))
        .addToBackStack(SettingsWalletsFragment::class.java.simpleName)
        .commit()
  }
}

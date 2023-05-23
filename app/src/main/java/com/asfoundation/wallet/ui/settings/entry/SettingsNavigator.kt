package com.asfoundation.wallet.ui.settings.entry

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.backup.BackupActivity
import com.asfoundation.wallet.eskills.withdraw.WithdrawActivity
import com.asfoundation.wallet.manage_wallets.ManageWalletFragment
import com.asfoundation.wallet.promo_code.bottom_sheet.entry.PromoCodeBottomSheetFragment
import com.asfoundation.wallet.recover.RecoverActivity
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import com.asfoundation.wallet.ui.settings.wallets.SettingsWalletsFragment
import com.asfoundation.wallet.ui.wallets.WalletsModel
import com.asfoundation.wallet.redeem_gift.bottom_sheet.RedeemGiftBottomSheetFragment
import javax.inject.Inject

class SettingsNavigator @Inject constructor(
  private val fragmentManager: FragmentManager,
  private val activity: FragmentActivity
) {

  companion object {
    private const val AUTHENTICATION_REQUEST_CODE = 33
  }

  fun showAuthentication() {
    val intent = AuthenticationPromptActivity.newIntent(activity)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    activity.startActivityForResult(intent, AUTHENTICATION_REQUEST_CODE)
  }

  fun navigateToManageWallet() {
    fragmentManager.beginTransaction()
      .setCustomAnimations(
        R.anim.fade_in_animation, R.anim.fragment_fade_out_animation,
        R.anim.fade_in_animation, R.anim.fragment_fade_out_animation
      )
      .replace(R.id.container, ManageWalletFragment())
      .addToBackStack(ManageWalletFragment::class.java.simpleName)
      .commit()
  }

  fun navigateToBackup(walletAddress: String) {
    activity.startActivity(
      BackupActivity.newIntent(
        activity,
        walletAddress,
        isBackupTrigger = false
      )
    )
  }

  fun showWalletsBottomSheet(walletModel: WalletsModel) {
    fragmentManager.beginTransaction()
      .setCustomAnimations(
        R.anim.fade_in_animation, R.anim.fragment_slide_down,
        R.anim.fade_in_animation, R.anim.fragment_slide_down
      )
      .replace(
        R.id.bottom_sheet_fragment_container,
        SettingsWalletsFragment.newInstance(walletModel)
      )
      .addToBackStack(SettingsWalletsFragment::class.java.simpleName)
      .commit()
  }

  fun showPromoCodeFragment() {
    PromoCodeBottomSheetFragment.newInstance()
      .show(fragmentManager, "PromoCodeBottomSheet")
  }

  fun showRedeemGiftFragment() {
    RedeemGiftBottomSheetFragment.newInstance()
      .show(fragmentManager, "RedeemGiftBottomSheet")
  }

  fun navigateToRecoverWalletActivity() {
    activity.startActivity(RecoverActivity.newIntent(activity, onboardingLayout = false))
  }

  fun navigateToWithdrawScreen() {
    activity.startActivity(WithdrawActivity.newIntent(activity))
  }
}

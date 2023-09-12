package com.asfoundation.wallet.wallet.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.asf.wallet.R
import com.asfoundation.wallet.backup.BackupWalletEntryFragment.Companion.WALLET_ADDRESS_KEY
import com.asfoundation.wallet.backup.BackupWalletEntryFragment.Companion.WALLET_NAME
import com.asfoundation.wallet.rating.RatingActivity
import com.asfoundation.wallet.recover.RecoverActivity
import com.asfoundation.wallet.topup.TopUpActivity
import com.asfoundation.wallet.ui.settings.entry.SettingsFragment
import com.asfoundation.wallet.wallet.home.bottom_sheet.HomeManageWalletBottomSheetFragment
import javax.inject.Inject

class HomeNavigator
@Inject
constructor(
  private val fragment: Fragment,
) : Navigator {

  fun navigateToRateUs(shouldNavigate: Boolean) {
    if (shouldNavigate) {
      val intent = RatingActivity.newIntent(fragment.requireContext())
      openIntent(intent)
    }
  }

  fun navigateToBrowser(uri: Uri) {
    try {
      val launchBrowser = Intent(Intent.ACTION_VIEW, uri)
      launchBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      fragment.requireContext().startActivity(launchBrowser)
    } catch (exception: ActivityNotFoundException) {
      exception.printStackTrace()
      Toast.makeText(fragment.requireContext(), R.string.unknown_error, Toast.LENGTH_SHORT).show()
    }
  }

  fun navigateToBackup(
    walletAddress: String,
    walletName: String,
    mainNavController: NavController
  ) {
    val bundle = Bundle()
    bundle.putString(WALLET_ADDRESS_KEY, walletAddress)
    bundle.putString(WALLET_NAME, walletName)
    mainNavController.navigate(R.id.action_navigate_to_backup_entry_wallet, args = bundle)
  }

  fun navigateToRecoverWallet() {
    val intent =
      RecoverActivity.newIntent(fragment.requireContext(), onboardingLayout = false).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
      }
    openIntent(intent)
  }

  fun navigateToTopUp() {
    val intent =
      TopUpActivity.newIntent(fragment.requireContext()).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
      }
    fragment.requireContext().startActivity(intent)
  }

  fun navigateToCurrencySelector(mainNavController: NavController) {
    mainNavController.navigate(R.id.action_navigate_to_change_fiat_currency)
  }

  fun navigateToManageBottomSheet() {
    val bottomSheet = HomeManageWalletBottomSheetFragment.newInstance()
    bottomSheet.show(fragment.parentFragmentManager, "HomeManageWallet")
  }

  fun navigateToTransfer(mainNavController: NavController) {
    mainNavController.navigate(R.id.action_navigate_to_send_funds)
  }

  fun navigateToNfts(mainNavController: NavController) {
    mainNavController.navigate(R.id.action_navigate_to_nfts)
  }

  fun navigateToSettings(mainNavController: NavController, turnOnFingerprint: Boolean = false) {
    val bundle = Bundle()
    bundle.putBoolean(SettingsFragment.TURN_ON_FINGERPRINT, turnOnFingerprint)
    mainNavController.navigate(resId = R.id.action_navigate_to_settings, args = bundle)
  }

  fun openIntent(intent: Intent) = fragment.requireContext().startActivity(intent)
}

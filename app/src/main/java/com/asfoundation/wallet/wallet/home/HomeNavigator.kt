package com.asfoundation.wallet.wallet.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.appcoins.wallet.sharedpreferences.BackupTriggerPreferencesDataSource
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.feature.backup.ui.BackupActivity
import com.appcoins.wallet.feature.backup.ui.triggers.BackupTriggerDialogFragment
import com.asf.wallet.R
import com.asfoundation.wallet.backup.BackupWalletEntryFragment.Companion.WALLET_ADDRESS_KEY
import com.asfoundation.wallet.main.nav_bar.NavBarFragmentNavigator
import com.asfoundation.wallet.rating.RatingActivity
import com.asfoundation.wallet.recover.RecoverActivity
import com.asfoundation.wallet.topup.TopUpActivity
import com.asfoundation.wallet.transactions.TransactionDetailsFragment
import com.asfoundation.wallet.transactions.TransactionModel
import com.asfoundation.wallet.ui.settings.entry.SettingsFragment
import com.asfoundation.wallet.ui.transact.TransferActivity
import javax.inject.Inject

class HomeNavigator
@Inject
constructor(
  private val fragment: Fragment,
  private val navBarFragmentNavigator: NavBarFragmentNavigator
) : Navigator {

  fun navigateToRateUs(shouldNavigate: Boolean) {
    if (shouldNavigate) {
      val intent = RatingActivity.newIntent(fragment.requireContext())
      openIntent(intent)
    }
  }

  fun navigateToReward() {
    navBarFragmentNavigator.navigateToRewards()
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

  fun navigateToTransactionDetails(navController: NavController, transaction: TransactionModel) {
    val bundle = Bundle()
    bundle.putParcelable(TransactionDetailsFragment.TRANSACTION_KEY, transaction)
    navController.navigate(resId = R.id.action_navigate_to_transaction_details, args = bundle)
  }

  fun navigateToBackup(
    walletAddress: String,
    mainNavController: NavController
  ) {
    val bundle = Bundle()
    bundle.putString(WALLET_ADDRESS_KEY, walletAddress)
    mainNavController.navigate(R.id.action_navigate_to_backup_entry_wallet, args = bundle)
  }

  fun navigateToRecoverWallet() {
    val intent = RecoverActivity.newIntent(fragment.requireContext(), onboardingLayout = false)
      .apply {
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

  fun navigateToBackupTrigger(
    walletAddress: String,
    triggerSource: BackupTriggerPreferencesDataSource.TriggerSource
  ) {
    val bottomSheet = BackupTriggerDialogFragment.newInstance(walletAddress, triggerSource)
    bottomSheet.isCancelable = false
    bottomSheet.show(fragment.parentFragmentManager, "BackupTrigger")
  }

  fun navigateToCurrencySelector(
    mainNavController: NavController
  ) {
    mainNavController.navigate(R.id.action_navigate_to_change_fiat_currency)
  }


  fun navigateToTransfer() {
    val intent = TransferActivity.newIntent(fragment.requireContext())
    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    openIntent(intent)
  }

  fun navigateToNfts(mainNavController: NavController) {
    mainNavController.navigate(R.id.action_navigate_to_nfts)
  }

  fun navigateToManageWallet(
    mainNavController: NavController
  ) {
    mainNavController.navigate(R.id.action_navigate_to_manage_wallet)
  }

  fun navigateToSettings(
    mainNavController: NavController,
    turnOnFingerprint: Boolean = false
  ) {
    val bundle = Bundle()
    bundle.putBoolean(SettingsFragment.TURN_ON_FINGERPRINT, turnOnFingerprint)
    mainNavController.navigate(resId = R.id.action_navigate_to_settings, args = bundle)
  }

  fun openIntent(intent: Intent) = fragment.requireContext()
    .startActivity(intent)
}

package com.asfoundation.wallet.wallet.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.appcoins.wallet.sharedpreferences.BackupTriggerPreferencesDataSource
import com.appcoins.wallet.ui.arch.data.Navigator
import com.appcoins.wallet.ui.arch.data.navigate
import com.asf.wallet.R
import com.asfoundation.wallet.C
import com.asfoundation.wallet.backup.BackupActivity
import com.asfoundation.wallet.backup.triggers.BackupTriggerDialogFragment
import com.asfoundation.wallet.change_currency.ChangeFiatCurrencyActivity
import com.asfoundation.wallet.main.nav_bar.NavBarFragmentNavigator
import com.asfoundation.wallet.my_wallets.main.MyWalletsFragmentDirections
import com.asfoundation.wallet.rating.RatingActivity
import com.asfoundation.wallet.recover.RecoverActivity
import com.asfoundation.wallet.topup.TopUpActivity
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.balance.TransactionDetailActivity
import com.asfoundation.wallet.ui.settings.SettingsActivity
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

  fun navigateToSettings(turnOnFingerprint: Boolean = false) {
    val intent = SettingsActivity.newIntent(fragment.requireContext(), turnOnFingerprint)
    openIntent(intent)
  }

  fun navigateToReward() {
    navBarFragmentNavigator.navigateToRewards()
  }

  fun navigateToPromotions() {
    navBarFragmentNavigator.navigateToPromotions()
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

  fun handleShare(link: String) {
    ShareCompat.IntentBuilder.from(fragment.activity as BaseActivity)
      .setText(link)
      .setType("text/plain")
      .setChooserTitle(fragment.resources.getString(R.string.referral_share_sheet_title))
      .startChooser()
  }

  fun navigateToTransactionDetails(transaction: Transaction, globalBalanceCurrency: String) {
    with(fragment.requireContext()) {
      val intent =
        Intent(this, TransactionDetailActivity::class.java).apply {
          putExtra(C.Key.TRANSACTION, transaction)
          putExtra(C.Key.GLOBAL_BALANCE_CURRENCY, globalBalanceCurrency)
        }
      startActivity(intent)
    }
  }

  fun navigateToBackup(walletAddress: String) {
    val intent =
      BackupActivity.newIntent(fragment.requireContext(), walletAddress, isBackupTrigger = false)
        .apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }
    openIntent(intent)
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

  fun navigateToCurrencySelector() {
    val intent =
      ChangeFiatCurrencyActivity.newIntent(fragment.requireContext()).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
      }
    openIntent(intent)
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

  fun navigateToTransactionsList(
    mainNavController: NavController
  ) {
    mainNavController.navigate(R.id.action_navigate_to_transactions_list)
  }

  fun openIntent(intent: Intent) = fragment.requireContext()
    .startActivity(intent)
}

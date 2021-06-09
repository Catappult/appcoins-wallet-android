package com.asfoundation.wallet.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asfoundation.wallet.C
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.main.MainActivityNavigator
import com.asfoundation.wallet.rating.RatingActivity
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.MyAddressActivity
import com.asfoundation.wallet.ui.backup.BackupActivity
import com.asfoundation.wallet.ui.balance.TransactionDetailActivity
import com.asfoundation.wallet.ui.settings.SettingsActivity
import com.asfoundation.wallet.ui.transact.TransferActivity.Companion.newIntent

class HomeNavigator(private val fragment: Fragment,
                    private val mainActivityNavigator: MainActivityNavigator) : Navigator {

  fun navigateToRateUs(shouldNavigate: Boolean) {
    if (shouldNavigate) {
      fragment.requireContext()
          .startActivity(RatingActivity.newIntent(fragment.requireContext()))
    }
  }

  fun navigateToSettings(turnOnFingerprint: Boolean = false) {
    fragment.requireContext()
        .startActivity(SettingsActivity.newIntent(fragment.requireContext(), turnOnFingerprint))
  }

  fun navigateToMyWallets() {
    mainActivityNavigator.navigateToMyWallets()
  }

  fun navigateToSend() {
    val intent = newIntent(fragment.requireContext())
    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    fragment.requireContext()
        .startActivity(intent)
  }

  fun navigateToReceive(wallet: Wallet?) {
    if (wallet != null) {
      val intent = Intent(fragment.requireContext(), MyAddressActivity::class.java)
      intent.putExtra(C.Key.WALLET, wallet)
      intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
      fragment.requireContext()
          .startActivity(intent)
    }
  }

  fun navigateToBrowser(uri: Uri) {
    try {
      val launchBrowser = Intent(Intent.ACTION_VIEW, uri)
      launchBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      fragment.requireContext()
          .startActivity(launchBrowser)
    } catch (exception: ActivityNotFoundException) {
      exception.printStackTrace()
      Toast.makeText(fragment.requireContext(), R.string.unknown_error, Toast.LENGTH_SHORT)
          .show()
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
      val intent = Intent(this, TransactionDetailActivity::class.java)
          .apply {
            putExtra(C.Key.TRANSACTION, transaction)
            putExtra(C.Key.GLOBAL_BALANCE_CURRENCY, globalBalanceCurrency)
          }
      startActivity(intent)
    }
  }

  fun navigateToBackup(walletAddress: String) {
    val intent = BackupActivity.newIntent(fragment.requireContext(), walletAddress)
        .apply {
          flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
    fragment.requireContext()
        .startActivity(intent)
  }

  fun openIntent(intent: Intent) = fragment.requireContext()
      .startActivity(intent)
}

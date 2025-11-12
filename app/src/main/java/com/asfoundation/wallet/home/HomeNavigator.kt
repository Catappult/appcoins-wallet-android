package com.asfoundation.wallet.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletBalance
import com.asf.wallet.R
import com.asfoundation.wallet.backup.BackupWalletEntryFragment.Companion.WALLET_ADDRESS_KEY
import com.asfoundation.wallet.backup.BackupWalletEntryFragment.Companion.WALLET_NAME
import com.asfoundation.wallet.home.bottom_sheet.HomeDetailsBalanceBottomSheetFragment
import com.asfoundation.wallet.manage_wallets.bottom_sheet.ManageWalletBalanceBottomSheetFragment
import com.asfoundation.wallet.rating.RatingActivity
import com.asfoundation.wallet.recover.RecoverActivity
import com.asfoundation.wallet.topup.TopUpActivity
import com.asfoundation.wallet.ui.settings.entry.SettingsFragment
import com.asfoundation.wallet.home.bottom_sheet.HomeManageWalletBottomSheetFragment
import com.asfoundation.wallet.promo_code.bottom_sheet.entry.PromoCodeBottomSheetFragment
import javax.inject.Inject

class HomeNavigator
@Inject
constructor(
  private val fragment: Fragment,
) : Navigator {

  companion object {
    /**
     * Key to associate the Binder responsible for
     * process the login response in the [HomeManageWalletBottomSheetFragment].
     */
    const val RESULT_LAUNCHER_BINDER = "snackBar binder"
  }

  /**
   * [Binder] responsible for process the login response in the [HomeManageWalletBottomSheetFragment]
   * and [SettingsFragment].
   *
   * This Binder is passed in the [Bundle] when the navigation is triggered.
   */
  internal class HomeFragmentBinder(
    val resultLauncher: ActivityResultLauncher<Intent>
  ) : Binder()

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

  fun navigateToBalanceBottomSheet(walletBalance: WalletBalance) {
    val bottomSheet = ManageWalletBalanceBottomSheetFragment.newInstance()
    val bundle = Bundle()
    bundle.putSerializable(
      ManageWalletBalanceBottomSheetFragment.WALLET_BALANCE_MODEL,
      walletBalance
    )
    bottomSheet.arguments = bundle
    bottomSheet.show(fragment.parentFragmentManager, "HomeBalanceWallet")
  }

  fun navigateToManageBottomSheet(
    canTransfer: Boolean,
    isLoggedIn: Boolean,
    resultLauncher: ActivityResultLauncher<Intent>
  ) {
    val bottomSheet = HomeManageWalletBottomSheetFragment.newInstance()
    val bundle = Bundle()
    bundle.putBoolean(
      HomeManageWalletBottomSheetFragment.CAN_TRANSFER,
      canTransfer
    )
    bundle.putBoolean(
      HomeManageWalletBottomSheetFragment.IS_LOGGED_IN,
      isLoggedIn
    )
    bundle.putBinder(RESULT_LAUNCHER_BINDER, HomeFragmentBinder(resultLauncher))
    bottomSheet.arguments = bundle
    bottomSheet.show(fragment.parentFragmentManager, "HomeManageWallet")
  }

  fun navigateToDetailsBalanceBottomSheet(balanceValue: String, balanceCurrency: String) {
    val bundle = Bundle()
    val bottomSheet = HomeDetailsBalanceBottomSheetFragment.newInstance()
    bundle.putString(HomeDetailsBalanceBottomSheetFragment.BALANCE_VALUE, balanceValue)
    bundle.putString(HomeDetailsBalanceBottomSheetFragment.BALANCE_CURRENCY, balanceCurrency)
    bottomSheet.arguments = bundle
    bottomSheet.show(fragment.parentFragmentManager, "HomeDetailsBalanceBottomSheetFragment")
  }

  fun navigateToPromoCode(promoCode: String? = null) {
    val bottomSheet = PromoCodeBottomSheetFragment.newInstance()
    val bundle = Bundle()
    bundle.putString(
      PromoCodeBottomSheetFragment.EXTRA_PROMO_CODE,
      promoCode
    )
    bottomSheet.arguments = bundle
    bottomSheet.show(fragment.parentFragmentManager, "HomePromoCode")
  }

  fun navigateToTransfer(mainNavController: NavController) {
    mainNavController.navigate(R.id.action_navigate_to_send_funds)
  }

  fun navigateToSettings(
    mainNavController: NavController,
    turnOnFingerprint: Boolean = false,
    launcher: ActivityResultLauncher<Intent>
  ) {
    val bundle = Bundle()
    bundle.putBoolean(SettingsFragment.TURN_ON_FINGERPRINT, turnOnFingerprint)
    bundle.putBinder(RESULT_LAUNCHER_BINDER, HomeFragmentBinder(launcher))
    mainNavController.navigate(resId = R.id.action_navigate_to_settings, args = bundle)
  }

  fun openIntent(intent: Intent) = fragment.requireContext().startActivity(intent)
}

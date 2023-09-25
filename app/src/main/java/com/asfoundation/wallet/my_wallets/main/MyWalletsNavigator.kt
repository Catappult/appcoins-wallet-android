package com.asfoundation.wallet.my_wallets.main

import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletBalance
import com.asf.wallet.R
import com.asfoundation.wallet.backup.BackupWalletEntryFragment
import com.asfoundation.wallet.backup.BackupWalletEntryFragment.Companion.WALLET_NAME
import com.asfoundation.wallet.manage_wallets.bottom_sheet.ChangeActiveWalletBottomSheetFragment
import com.asfoundation.wallet.manage_wallets.bottom_sheet.ManageWalletBalanceBottomSheetFragment
import com.asfoundation.wallet.manage_wallets.bottom_sheet.ManageWalletBalanceBottomSheetFragment.Companion.WALLET_BALANCE_MODEL
import com.asfoundation.wallet.manage_wallets.bottom_sheet.ManageWalletBottomSheetFragment
import com.asfoundation.wallet.manage_wallets.bottom_sheet.ManageWalletNameBottomSheetFragment
import com.asfoundation.wallet.transfers.TransferFundsFragment
import com.asfoundation.wallet.ui.bottom_navigation.TransferDestinations
import javax.inject.Inject

class MyWalletsNavigator
@Inject
constructor(private val fragment: Fragment, private val navController: NavController) : Navigator {
  fun navigateToMore(
    walletAddress: String,
    totalFiatBalance: String,
    appcoinsBalance: String,
    creditsBalance: String,
    ethereumBalance: String
  ) {
    navigate(
      navController,
      MyWalletsFragmentDirections.actionNavigateToMore(
        walletAddress, totalFiatBalance, appcoinsBalance, creditsBalance, ethereumBalance
      )
    )
  }

  fun navigateToManageWalletNameBottomSheet(walletAddress: String, walletName: String) {
    val bundle = Bundle()
    bundle.putString(ManageWalletNameBottomSheetFragment.WALLET_NAME, walletName)
    bundle.putString(ManageWalletNameBottomSheetFragment.WALLET_ADDRESS, walletAddress)
    val bottomSheet = ManageWalletNameBottomSheetFragment.newInstance()
    bottomSheet.arguments = bundle
    bottomSheet.show(fragment.parentFragmentManager, "ManageWalletName")
  }

  fun navigateToManageWalletBalanceBottomSheet(walletBalance: WalletBalance) {
    val bottomSheet = ManageWalletBalanceBottomSheetFragment.newInstance()
    val bundle = Bundle()
    bundle.putSerializable(WALLET_BALANCE_MODEL, walletBalance)
    bottomSheet.arguments = bundle
    bottomSheet.show(fragment.parentFragmentManager, "ManageWallet")
  }

  fun navigateToManageWalletBottomSheet(hasOneWallet: Boolean) {
    val bundle = Bundle()
    bundle.putBoolean(ManageWalletBottomSheetFragment.HAS_ONE_WALLET, hasOneWallet)
    val bottomSheet = ManageWalletBottomSheetFragment.newInstance()
    bottomSheet.arguments = bundle
    bottomSheet.show(fragment.parentFragmentManager, "ManageWallet")
  }

  fun navigateToChangeActiveWalletBottomSheet(
    walletAddress: String,
    walletName: String,
    walletBalance: String,
    walletBalanceSymbol: String
  ) {
    val bundle = Bundle()
    val bottomSheet = ChangeActiveWalletBottomSheetFragment.newInstance()
    bundle.putString(ChangeActiveWalletBottomSheetFragment.WALLET_NAME, walletName)
    bundle.putString(ChangeActiveWalletBottomSheetFragment.WALLET_ADDRESS, walletAddress)
    bundle.putString(ChangeActiveWalletBottomSheetFragment.WALLET_BALANCE, walletBalance)
    bundle.putString(
      ChangeActiveWalletBottomSheetFragment.WALLET_BALANCE_SYMBOL, walletBalanceSymbol
    )
    bottomSheet.arguments = bundle
    bottomSheet.show(fragment.parentFragmentManager, "ManageWallet")
  }

  fun navigateToName(
    walletAddress: String,
    walletName: String,
  ) {
    navigate(
      navController, MyWalletsFragmentDirections.actionNavigateToName(walletAddress, walletName)
    )
  }

  fun navigateToBalanceDetails(
    totalFiatBalance: String,
    appcoinsBalance: String,
    creditsBalance: String,
    ethereumBalance: String
  ) {
    navigate(
      navController,
      MyWalletsFragmentDirections.actionNavigateToBalanceDetails(
        totalFiatBalance, appcoinsBalance, creditsBalance, ethereumBalance
      )
    )
  }

  fun navigateToReceive(navController: NavController, transferDestinations: TransferDestinations) {
    val bundle = Bundle()
    bundle.putInt(TransferFundsFragment.TRANSFER_KEY, transferDestinations.ordinal)
    navController.navigate(R.id.action_navigate_to_receive_funds, bundle)
  }

  fun navigateToNfts() {
    navigate(navController, MyWalletsFragmentDirections.actionNavigateToNfts())
  }

  fun navigateToVerifyPicker() {
    navigate(navController, MyWalletsFragmentDirections.actionNavigateToVerifyPicker())
  }

  fun navigateToVerifyCreditCard() {
    navigate(navController, MyWalletsFragmentDirections.actionNavigateToVerifyCreditCard(false))
  }

  fun navigateToBackup(walletAddress: String, walletName: String) {
    val bundle = Bundle()
    bundle.putString(BackupWalletEntryFragment.WALLET_ADDRESS_KEY, walletAddress)
    bundle.putString(WALLET_NAME, walletName)
    navController.navigate(R.id.action_navigate_to_backup_entry_wallet, args = bundle)
  }

  fun navigateToQrCode(qrCodeView: View) {
    val options =
      ActivityOptionsCompat.makeSceneTransitionAnimation(
        fragment.requireActivity(), Pair(qrCodeView, "qr_code_image")
      )
    val extras = ActivityNavigatorExtras(options)
    navController.navigate(R.id.action_navigate_to_qr_code, null, null, extras)
  }

  fun navigateBack() {
    navController.popBackStack()
  }
}

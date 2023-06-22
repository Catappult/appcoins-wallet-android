package com.asfoundation.wallet.my_wallets.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.asf.wallet.R
import com.asfoundation.wallet.transfers.TransferFundsFragment
import com.asfoundation.wallet.ui.bottom_navigation.TransferDestinations
import javax.inject.Inject

class MyWalletsNavigator @Inject constructor(
  private val fragment: Fragment,
  private val navController: NavController
) : Navigator {

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
        walletAddress,
        totalFiatBalance,
        appcoinsBalance,
        creditsBalance,
        ethereumBalance
      )
    )
  }

  fun navigateToName(
    walletAddress: String,
    walletName: String,
  ) {
    navigate(
      navController,
      MyWalletsFragmentDirections.actionNavigateToName(walletAddress, walletName)
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
        totalFiatBalance,
        appcoinsBalance,
        creditsBalance,
        ethereumBalance
      )
    )
  }

//  fun navigateToSend() {
//    val intent = TransferActivity.newIntent(fragment.requireContext())
//    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
//    openIntent(intent)
//  }

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

  fun navigateToBackupWallet(walletAddress: String) {
    navigate(navController, MyWalletsFragmentDirections.actionNavigateToBackupWallet(walletAddress))
  }

  fun navigateToQrCode(qrCodeView: View) {
    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
      fragment.requireActivity(),
      Pair(qrCodeView, "qr_code_image")
    )
    val extras = ActivityNavigatorExtras(options)
    navController.navigate(R.id.action_navigate_to_qr_code, null, null, extras)
  }

  fun navigateToRemoveWallet(navController: NavController) {
    navController.navigate(R.id.action_navigate_to_remove_wallet)
  }

  private fun openIntent(intent: Intent) = fragment.requireContext()
    .startActivity(intent)
}
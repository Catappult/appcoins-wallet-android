package com.asfoundation.wallet.my_wallets.main

import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.NavController
import com.asf.wallet.R
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import com.asfoundation.wallet.ui.wallets.WalletBalance
import javax.inject.Inject

class MyWalletsNavigator @Inject constructor(private val fragment: Fragment,
                         private val navController: NavController) : Navigator {

  fun navigateToChangeActiveWallet(walletBalance: WalletBalance) {
    navigate(navController,
        MyWalletsFragmentDirections.actionNavigateToChangeActiveWallet(walletBalance))
  }

  fun navigateToCreateNewWallet() {
    navigate(navController, MyWalletsFragmentDirections.actionNavigateToCreateWallet())
  }

  fun navigateToTokenInfo(title: String, image: String, description: String, showTopUp: Boolean) {
    navigate(navController,
        MyWalletsFragmentDirections.actionNavigateToTokenInfo(title, image, description,
            showTopUp))
  }

  fun navigateToMore(walletAddress: String, totalFiatBalance: String,
                     appcoinsBalance: String, creditsBalance: String,
                     ethereumBalance: String, showVerifyCard: Boolean, showDeleteWallet: Boolean) {
    navigate(navController,
        MyWalletsFragmentDirections.actionNavigateToMore(walletAddress, totalFiatBalance,
            appcoinsBalance, creditsBalance, ethereumBalance, showVerifyCard, showDeleteWallet))
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
    navigate(navController,
        MyWalletsFragmentDirections.actionNavigateToBackupWallet(walletAddress))
  }

  fun navigateToQrCode(qrCodeView: View) {
    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(fragment.requireActivity(),
        Pair(qrCodeView, "qr_code_image"))
    val extras = ActivityNavigatorExtras(options)
    navController.navigate(R.id.action_navigate_to_qr_code, null, null, extras)
  }
}
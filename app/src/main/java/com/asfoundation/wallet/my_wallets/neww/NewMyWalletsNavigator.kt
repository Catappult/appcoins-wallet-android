package com.asfoundation.wallet.my_wallets.neww

import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.NavController
import com.asf.wallet.R
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import com.asfoundation.wallet.ui.wallets.WalletBalance

class NewMyWalletsNavigator(private val fragment: NewMyWalletsFragment,
                            private val navController: NavController) : Navigator {

  fun navigateToChangeActiveWallet(walletBalance: WalletBalance) {
    navigate(navController,
        NewMyWalletsFragmentDirections.actionNavigateToChangeActiveWallet(walletBalance))
  }

  fun navigateToCreateNewWallet() {
    navigate(navController, NewMyWalletsFragmentDirections.actionNavigateToCreateWallet())
  }

  fun navigateToTokenInfo(title: String, image: String, description: String, showTopUp: Boolean) {
    navigate(navController,
        NewMyWalletsFragmentDirections.actionNavigateToTokenInfo(title, image, description,
            showTopUp))
  }

  fun navigateToMore(walletAddress: String, totalFiatBalance: String,
                     appcoinsBalance: String, creditsBalance: String,
                     ethereumBalance: String, showDeleteWallet: Boolean) {
    navigate(navController,
        NewMyWalletsFragmentDirections.actionNavigateToMore(walletAddress, totalFiatBalance,
            appcoinsBalance, creditsBalance, ethereumBalance, showDeleteWallet))
  }

  fun navigateToVerify() {
    navigate(navController, NewMyWalletsFragmentDirections.actionNavigateToVerify())
  }

  fun navigateToBackupWallet(walletAddress: String) {
    navigate(navController,
        NewMyWalletsFragmentDirections.actionNavigateToBackupWallet(walletAddress))
  }

  fun navigateToQrCode(qrCodeView: View) {
    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(fragment.requireActivity(),
        Pair(qrCodeView, "qr_code_image"))
    val extras = ActivityNavigatorExtras(options)
    navController.navigate(R.id.action_navigate_to_qr_code, null, null, extras)
  }

//  val intent = TokenDetailsActivity.newInstance(fragment.requireContext(), tokenDetailsId)
//
//  val options = ActivityOptionsCompat.makeSceneTransitionAnimation(fragment.requireActivity(),
//      androidx.core.util.Pair(imgView, ViewCompat.getTransitionName(imgView)!!),
//      androidx.core.util.Pair(textView, ViewCompat.getTransitionName(textView)!!),
//      androidx.core.util.Pair(parentView,
//          ViewCompat.getTransitionName(parentView)!!))
}
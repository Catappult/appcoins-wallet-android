package com.asfoundation.wallet.my_wallets

import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.main.MainActivityNavigator
import com.asfoundation.wallet.restore.RestoreWalletActivity
import com.asfoundation.wallet.ui.backup.BackupActivity
import com.asfoundation.wallet.ui.balance.TokenDetailsActivity
import com.asfoundation.wallet.ui.wallets.RemoveWalletActivity
import com.asfoundation.wallet.ui.wallets.WalletDetailsFragment
import com.asfoundation.wallet.verification.VerificationActivity

class MyWalletsNavigator(private val fragment: Fragment,
                         private val mainActivityNavigator: MainActivityNavigator) : Navigator {

  private val REQUEST_CODE = 123

  fun navigateToHome() {
    mainActivityNavigator.navigateToHome()
  }

  fun navigateBackToMyWallets() {
    fragment.requireActivity().supportFragmentManager.beginTransaction()
        .replace(R.id.nav_host_container, MyWalletsFragment.newInstance())
        .commit()
  }

  fun showTokenDetailsScreen(
      tokenDetailsId: TokenDetailsActivity.TokenDetailsId, imgView: ImageView,
      textView: TextView, parentView: View) {

    val intent = TokenDetailsActivity.newInstance(fragment.requireContext(), tokenDetailsId)

    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(fragment.requireActivity(),
        androidx.core.util.Pair(imgView, ViewCompat.getTransitionName(imgView)!!),
        androidx.core.util.Pair(textView, ViewCompat.getTransitionName(textView)!!),
        androidx.core.util.Pair(parentView,
            ViewCompat.getTransitionName(parentView)!!))

    fragment.requireContext()
        .startActivity(intent, options.toBundle())
  }

  fun navigateToWalletDetailView(walletAddress: String, isActive: Boolean) {
    fragment.requireActivity().supportFragmentManager.beginTransaction()
        .replace(R.id.nav_host_container,
            WalletDetailsFragment.newInstance(walletAddress, isActive))
        .addToBackStack(WalletDetailsFragment::class.java.simpleName)
        .commit()
  }

  fun navigateToVerificationView() {
    fragment.context?.let {
      val intent = VerificationActivity.newIntent(it)
          .apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
          }
      fragment.requireContext()
          .startActivity(intent)
    }
  }

  fun navigateToRemoveWalletView(walletAddress: String, totalFiatBalance: String,
                                 appcoinsBalance: String, creditsBalance: String,
                                 ethereumBalance: String) {
    fragment.requireActivity()
        .startActivityForResult(
            RemoveWalletActivity.newIntent(fragment.requireContext(), walletAddress,
                totalFiatBalance, appcoinsBalance,
                creditsBalance, ethereumBalance), REQUEST_CODE)
  }

  fun navigateToBackupView(walletAddress: String) {
    fragment.requireContext()
        .startActivity(BackupActivity.newIntent(fragment.requireContext(), walletAddress))
  }

  fun navigateToRestoreView() {
    fragment.requireContext()
        .startActivity(RestoreWalletActivity.newIntent(fragment.requireContext()))
  }


}
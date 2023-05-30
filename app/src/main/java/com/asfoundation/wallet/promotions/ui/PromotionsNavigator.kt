package com.asfoundation.wallet.promotions.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asf.wallet.R
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.asfoundation.wallet.referrals.InviteFriendsActivity
import com.asfoundation.wallet.ui.gamification.GamificationActivity
import com.wallet.appcoins.core.legacy_base.BaseActivity
import javax.inject.Inject


class PromotionsNavigator @Inject constructor(private val fragment: Fragment) :
  Navigator {

  fun navigateToInfo() {
    navigate(fragment.findNavController(), PromotionsFragmentDirections.actionNavigateToInfo())
  }

  fun navigateToGamification(cachedBonus: Double) {
    fragment.startActivity(GamificationActivity.newIntent(fragment.requireContext(), cachedBonus))
  }

  fun navigateToInviteFriends() {
    val intent = Intent(fragment.requireContext(), InviteFriendsActivity::class.java)
    fragment.startActivity(intent)
  }

  fun navigateToVipReferral(
    bonus: String,
    code: String,
    totalEarned: String,
    numberReferrals: String
  ) {
    navigate(fragment.findNavController(), PromotionsFragmentDirections.actionNavigateToVipReferral(bonus, code, totalEarned, numberReferrals))
  }

  fun handleShare(link: String) {
    ShareCompat.IntentBuilder.from(fragment.activity as BaseActivity)
      .setText(link)
      .setType("text/plain")
      .setChooserTitle(fragment.resources.getString(R.string.referral_share_sheet_title))
      .startChooser()
  }

  @Throws(ActivityNotFoundException::class)
  fun openDetailsLink(detailsLink: String) {
    val uri = Uri.parse(detailsLink)
    val launchBrowser = Intent(Intent.ACTION_VIEW, uri)
    launchBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    fragment.startActivity(launchBrowser)
  }

  fun navigateToVoucherDetails(packageName: String) {
    //TODO
    Log.d("PromotionsNavigator", "Tried to navigate $packageName")
  }
}

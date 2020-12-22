package com.asfoundation.wallet.promotions

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asfoundation.wallet.referrals.InviteFriendsActivity
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.gamification.GamificationActivity

class PromotionsNavigator(private val fragment: Fragment) {

  fun navigateToGamification(cachedBonus: Double) {
    fragment.startActivity(GamificationActivity.newIntent(fragment.requireContext(), cachedBonus))
  }

  fun navigateToInviteFriends() {
    val intent = Intent(fragment.requireContext(), InviteFriendsActivity::class.java)
    fragment.startActivity(intent)
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
}

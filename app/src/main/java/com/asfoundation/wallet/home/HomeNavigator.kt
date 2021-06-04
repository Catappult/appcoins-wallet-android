package com.asfoundation.wallet.promotions.ui

import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.rating.RatingActivity
import com.asfoundation.wallet.ui.BaseActivity

class HomeNavigator(private val fragment: Fragment) : Navigator {

  fun handleShare(link: String) {
    ShareCompat.IntentBuilder.from(fragment.activity as BaseActivity)
        .setText(link)
        .setType("text/plain")
        .setChooserTitle(fragment.resources.getString(R.string.referral_share_sheet_title))
        .startChooser()
  }

  fun navigateToRateUs(shouldNavigate: Boolean) {
    if (shouldNavigate) {
      fragment.startActivity(RatingActivity.newIntent(fragment.requireContext()))
    }
  }

}

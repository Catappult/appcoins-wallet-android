package com.asfoundation.wallet.promotions

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asfoundation.wallet.promotions.voucher.VoucherDetailsFragment
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.referrals.InviteFriendsActivity
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.gamification.GamificationActivity
import com.asfoundation.wallet.ui.iab.IabActivity
import java.math.BigDecimal

class PromotionsNavigator(private val fragment: Fragment, private val activity: Activity) {

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

  fun navigateToVoucherDetails(title: String, featureGraphic: String, icon: String,
                               maxBonus: Double, packageName: String, hasAppcoins: Boolean) {
    fragment.requireActivity().supportFragmentManager.beginTransaction()
        .addToBackStack(null)
        .replace(R.id.fragment_container,
            VoucherDetailsFragment.newInstance(title, featureGraphic, icon, maxBonus, packageName,
                hasAppcoins))
        .commit()
  }

  private fun navigateToPurchaseFlow(sku: String, title: String, fiatAmount: BigDecimal,
                                     fiatCurrency: String, fiatSymbol: String,
                                     appcAmount: BigDecimal, packageName: String) {
    val transaction =
        TransactionBuilder.createVoucherTransaction(sku, title, fiatAmount, fiatCurrency,
            fiatSymbol, appcAmount, packageName)
    fragment.startActivity(IabActivity.newIntent(activity, null, transaction, true, null))
  }
}

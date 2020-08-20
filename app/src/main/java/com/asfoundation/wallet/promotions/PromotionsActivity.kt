package com.asfoundation.wallet.promotions

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.ShareCompat
import com.asf.wallet.R
import com.asfoundation.wallet.referrals.InviteFriendsActivity
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.gamification.GamificationActivity

class PromotionsActivity : BaseActivity(), PromotionsActivityView {

  private lateinit var transactionsRouter: TransactionsRouter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_promotions)
    toolbar()
    transactionsRouter = TransactionsRouter()
    supportFragmentManager.beginTransaction()
        .add(R.id.fragment_container, PromotionsFragment.newInstance())
        .commit()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) transactionsRouter.open(this, true)
    return super.onOptionsItemSelected(item)
  }

  override fun navigateToLegacyGamification(bonus: Double) =
      startActivity(GamificationActivity.newIntent(this, true, bonus))

  override fun navigateToGamification(bonus: Double) =
      startActivity(GamificationActivity.newIntent(this, false, bonus))

  override fun handleShare(link: String) {
    ShareCompat.IntentBuilder.from(this)
        .setText(link)
        .setType("text/plain")
        .setChooserTitle(resources.getString(R.string.referral_share_sheet_title))
        .startChooser()
  }

  override fun navigateToInviteFriends() {
    val intent = Intent(this, InviteFriendsActivity::class.java)
    startActivity(intent)
  }
}

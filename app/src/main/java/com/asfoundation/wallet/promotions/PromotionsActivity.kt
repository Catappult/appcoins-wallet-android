package com.asfoundation.wallet.promotions

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.ShareCompat
import com.asf.wallet.R
import com.asfoundation.wallet.referrals.InviteFriendsActivity
import com.asfoundation.wallet.router.RewardsLevelRouter
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.ui.BaseActivity

class PromotionsActivity : BaseActivity(), PromotionsActivityView {

  private lateinit var transactionsRouter: TransactionsRouter
  private lateinit var rewardsLevelRouter: RewardsLevelRouter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.promotions_activity_view)
    toolbar()
    transactionsRouter = TransactionsRouter()
    rewardsLevelRouter = RewardsLevelRouter()
    supportFragmentManager.beginTransaction()
        .add(R.id.fragment_container, PromotionsFragment.newInstance())
        .commit()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        transactionsRouter.open(this, true)
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun navigateToLegacyGamification(bonus: Double) =
      rewardsLevelRouter.open(this, true, bonus)

  override fun navigateToGamification(bonus: Double) = rewardsLevelRouter.open(this, false, bonus)

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

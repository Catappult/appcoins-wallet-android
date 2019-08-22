package com.asfoundation.wallet.promotions

import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.ShareCompat
import com.asf.wallet.R
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
    val promotionsFragment = PromotionsFragment()
    supportFragmentManager.beginTransaction()
        .add(R.id.fragment_container, promotionsFragment)
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

  override fun navigateToGamification() {
    rewardsLevelRouter.open(this)
  }

  override fun handleShare() {
    ShareCompat.IntentBuilder.from(this)
        .setText("link")
        .setType("text/plain")
        .setChooserTitle(resources.getString(R.string.referral_share_sheet_title))
        .startChooser()
  }

  override fun navigateToPromotionDetails() {
    //TODO
  }
}

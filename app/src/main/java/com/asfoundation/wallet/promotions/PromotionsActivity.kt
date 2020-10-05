package com.asfoundation.wallet.promotions

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ShareCompat
import com.asf.wallet.R
import com.asfoundation.wallet.referrals.InviteFriendsActivity
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.gamification.GamificationActivity
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class PromotionsActivity : BaseActivity(), PromotionsActivityView {

  private lateinit var transactionsRouter: TransactionsRouter

  private var backEnabled = true

  private var onBackPressedSubject: PublishSubject<Any>? = null


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_promotions)
    toolbar()
    onBackPressedSubject = PublishSubject.create()
    transactionsRouter = TransactionsRouter()
    supportFragmentManager.beginTransaction()
        .add(R.id.fragment_container, PromotionsFragment.newInstance())
        .commit()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home)  {
      if (backEnabled) {
        transactionsRouter.open(this, true)
      } else {
        onBackPressedSubject?.onNext("")
      }
      return true
    } else {
      return super.onOptionsItemSelected(item)
    }
  }

  override fun navigateToGamification(bonus: Double) =
      startActivity(GamificationActivity.newIntent(this, bonus))

  override fun handleShare(link: String) {
    ShareCompat.IntentBuilder.from(this)
        .setText(link)
        .setType("text/plain")
        .setChooserTitle(resources.getString(R.string.referral_share_sheet_title))
        .startChooser()
  }

  override fun opendetailsLink(url: String) {
    try {
      val uri = Uri.parse(url)
      val launchBrowser = Intent(Intent.ACTION_VIEW, uri)
      launchBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      startActivity(launchBrowser)
    } catch (exception: ActivityNotFoundException) {
      exception.printStackTrace()
      Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT)
          .show()
    }
  }

  override fun navigateToInviteFriends() {
    val intent = Intent(this, InviteFriendsActivity::class.java)
    startActivity(intent)
  }

  override fun backPressed(): Observable<Any> {
    return onBackPressedSubject!!
  }

  override fun enableBack() {
    backEnabled = true
  }

  override fun disableBack() {
    backEnabled = false
  }
}

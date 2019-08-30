package com.asfoundation.wallet.referrals

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.router.ExternalBrowserRouter
import com.asfoundation.wallet.ui.BaseActivity
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import javax.inject.Inject

class InviteFriendsActivity : BaseActivity(), InviteFriendsActivityView {

  private lateinit var menu: Menu
  private lateinit var presenter: InviteFriendsActivityPresenter
  private lateinit var browserRouter: ExternalBrowserRouter
  private var infoButtonSubject: PublishSubject<Any>? = null
  private var infoButtonInitializeSubject: ReplaySubject<Boolean>? = null
  @Inject
  lateinit var smsValidationInteract: SmsValidationInteract
  @Inject
  lateinit var walletInteract: FindDefaultWalletInteract

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AndroidInjection.inject(this)
    setContentView(R.layout.invite_friends_activity_layout)
    toolbar()
    infoButtonSubject = PublishSubject.create()
    infoButtonInitializeSubject = ReplaySubject.create()
    browserRouter = ExternalBrowserRouter()
    navigateTo(LoadingFragment())
    presenter =
        InviteFriendsActivityPresenter(this, smsValidationInteract, walletInteract,
            CompositeDisposable(), Schedulers.io(), AndroidSchedulers.mainThread())
    presenter.present()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        super.onBackPressed()
      }
      R.id.action_info -> {
        infoButtonSubject?.onNext(Any())
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_info, menu)
    this.menu = menu
    infoButtonInitializeSubject?.onNext(true)
    return super.onCreateOptionsMenu(menu)
  }

  override fun navigateToVerificationFragment() {
    navigateTo(InviteFriendsVerificationFragment())
  }

  override fun navigateToInviteFriends() {
    navigateTo(InviteFriendsFragment())
  }

  override fun getInfoButtonClick(): Observable<Any> {
    return infoButtonSubject!!
  }

  override fun infoButtonInitialized(): Observable<Boolean> {
    return infoButtonInitializeSubject!!
  }

  override fun showNoNetworkScreen() {

  }

  override fun showInfoButton() {
    this.menu.findItem(R.id.action_info)
        .isVisible = true
  }

  override fun navigateToWalletValidation(beenInvited: Boolean) {

  }

  override fun navigateToTopApps() {
    browserRouter.open(this, Uri.parse(APTOIDE_TOP_APPS_URL))
  }

  override fun showShare(link: String) {
    ShareCompat.IntentBuilder.from(this)
        .setText(link)
        .setType("text/plain")
        .setChooserTitle(resources.getString(R.string.referral_share_sheet_title))
        .startChooser()
  }

  private fun navigateTo(fragment: Fragment) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .commit()
  }

  override fun onDestroy() {
    infoButtonSubject = null
    infoButtonInitializeSubject = null
    presenter.stop()
    super.onDestroy()
  }

  companion object {
    const val APTOIDE_TOP_APPS_URL = "https://en.aptoide.com/store/bds-store/group/group-10867"
  }
}
package com.asfoundation.wallet.referrals

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.invite_friends_fragment_layout.*
import kotlinx.android.synthetic.main.referral_notification_card.*
import javax.inject.Inject

class InviteFriendsFragment : DaggerFragment(), InviteFriendsFragmentView {

  private lateinit var presenter: InviteFriendsFragmentPresenter
  private lateinit var activity: InviteFriendsActivityView
  @Inject
  lateinit var referralInteractor: ReferralInteractorContract

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = InviteFriendsFragmentPresenter(this, referralInteractor, CompositeDisposable(),
        AndroidSchedulers.mainThread(), Schedulers.io())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    require(
        context is InviteFriendsActivityView) { InviteFriendsFragment::class.java.simpleName + " needs to be attached to a " + InviteFriendsActivity::class.java.simpleName }
    activity = context
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.invite_friends_fragment_layout, container, false)
  }

  override fun setTextValues(individualValue: String, pendingValue: String) {
    referral_description.text = getString(R.string.referral_view_verified_body, individualValue)
    notification_title.text =
        getString(R.string.referral_notification_bonus_pending_title, pendingValue)
  }

  override fun shareLinkClick(): Observable<Any> {
    return RxView.clicks(share_invite_button)
  }

  override fun appsAndGamesButtonClick(): Observable<Any> {
    return RxView.clicks(notification_apps_games_button)
  }

  override fun showShare() {
    activity.showShare()
  }

  override fun navigateToAptoide() {
    activity.navigateToTopApps()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}

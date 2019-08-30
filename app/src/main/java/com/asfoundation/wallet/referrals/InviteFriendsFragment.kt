package com.asfoundation.wallet.referrals

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.asf.wallet.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.invite_friends_fragment_layout.*
import kotlinx.android.synthetic.main.referral_notification_card.*
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class InviteFriendsFragment : DaggerFragment(), InviteFriendsFragmentView {

  private lateinit var presenter: InviteFriendsFragmentPresenter
  private lateinit var activity: InviteFriendsActivityView
  private lateinit var referralsBottomSheet: BottomSheetBehavior<View>
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
    referralsBottomSheet =
        BottomSheetBehavior.from(bottom_sheet_fragment_container)
    animateBackgroundFade()
    presenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    childFragmentManager.beginTransaction()
        .replace(R.id.bottom_sheet_fragment_container, ReferralsFragment())
        .commit()
    return inflater.inflate(R.layout.invite_friends_fragment_layout, container, false)
  }

  private fun animateBackgroundFade() {
    referralsBottomSheet.setBottomSheetCallback(object :
        BottomSheetBehavior.BottomSheetCallback() {
      override fun onStateChanged(bottomSheet: View, newState: Int) {
      }

      override fun onSlide(bottomSheet: View, slideOffset: Float) {
        background_fade_animation.progress = slideOffset
      }
    })
  }


  override fun setTextValues(individualValue: BigDecimal, pendingValue: BigDecimal,
                             currency: String) {
    referral_description.text =
        getString(R.string.referral_view_verified_body,
            currency + individualValue.setScale(2, RoundingMode.FLOOR).toString())
    notification_title.text =
        getString(R.string.referral_notification_bonus_pending_title,
            currency + pendingValue.setScale(2, RoundingMode.FLOOR).toString())
  }

  override fun shareLinkClick(): Observable<Any> {
    return RxView.clicks(share_invite_button)
  }

  override fun appsAndGamesButtonClick(): Observable<Any> {
    return RxView.clicks(notification_apps_games_button)
  }

  override fun showShare(link: String) {
    activity.showShare(link)
  }

  override fun navigateToAptoide() {
    activity.navigateToTopApps()
  }

  override fun showNotificationCard(pendingAmount: BigDecimal) {
    if (pendingAmount.toDouble() > 0) {
      referral_notification_card.visibility = VISIBLE
    } else {
      referral_notification_card.visibility = GONE
    }
  }

  private fun expandBottomSheet() {
    referralsBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
  }

  private fun collapseBottomSheet() {
    referralsBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
  }

  override fun changeBottomSheetState() {
    if (referralsBottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED) {
      expandBottomSheet()
    } else {
      collapseBottomSheet()
    }
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}

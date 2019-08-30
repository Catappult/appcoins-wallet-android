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
import kotlinx.android.synthetic.main.invite_friends_verification_layout.*
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class InviteFriendsVerificationFragment : DaggerFragment(), InviteFriendsVerificationView {

  private lateinit var presenter: InviteFriendsVerificationPresenter
  private lateinit var activity: InviteFriendsActivityView
  @Inject
  lateinit var referralInteractor: ReferralInteractorContract

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = InviteFriendsVerificationPresenter(this, referralInteractor, CompositeDisposable(),
        AndroidSchedulers.mainThread(), Schedulers.io())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    require(
        context is InviteFriendsActivityView) { InviteFriendsVerificationFragment::class.java.simpleName + " needs to be attached to a " + InviteFriendsActivity::class.java.simpleName }
    activity = context
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.invite_friends_verification_layout, container, false)
  }

  override fun setDescriptionText(referralValue: BigDecimal, currency: String) {
    verification_description.text =
        getString(R.string.referral_view_unverified_body,
            currency + referralValue.setScale(2, RoundingMode.FLOOR).toString())
  }

  override fun verifyButtonClick(): Observable<Any> {
    return RxView.clicks(verify_button)
  }

  override fun beenInvitedClick(): Observable<Any> {
    return RxView.clicks(invited_button)
  }

  override fun navigateToWalletValidation(beenInvited: Boolean) {
    activity.navigateToWalletValidation(beenInvited)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}

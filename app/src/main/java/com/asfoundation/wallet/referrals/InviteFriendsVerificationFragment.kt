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
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.invite_friends_verification_layout.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

class InviteFriendsVerificationFragment : DaggerFragment(), InviteFriendsVerificationView {

  private lateinit var presenter: InviteFriendsVerificationPresenter
  private lateinit var activity: InviteFriendsActivityView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = InviteFriendsVerificationPresenter(this, CompositeDisposable())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    require(
        context is InviteFriendsActivityView) { InviteFriendsVerificationFragment::class.java.simpleName + " needs to be attached to a " + InviteFriendsActivity::class.java.simpleName }
    activity = context
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setDescriptionText()
    presenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.invite_friends_verification_layout, container, false)
  }

  private fun setDescriptionText() {
    verification_description.text =
        getString(R.string.referral_view_unverified_body,
            currency + convertToString(amount))
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

  private fun convertToString(value: BigDecimal): String {
    val format = DecimalFormat("#.##")
    return format.format(value.setScale(2, RoundingMode.FLOOR))
  }

  val amount: BigDecimal by lazy {
    if (arguments!!.containsKey(AMOUNT)) {
      arguments!!.getSerializable(AMOUNT) as BigDecimal
    } else {
      throw IllegalArgumentException("Amount not found")
    }
  }

  val currency: String by lazy {
    if (arguments!!.containsKey(CURRENCY)) {
      arguments!!.getString(CURRENCY)
    } else {
      throw IllegalArgumentException("Currency not found")
    }
  }

  companion object {

    private const val AMOUNT = "amount"
    private const val CURRENCY = "currency"

    fun newInstance(amount: BigDecimal, currency: String): InviteFriendsVerificationFragment {
      val bundle = Bundle()
      bundle.putSerializable(AMOUNT, amount)
      bundle.putString(CURRENCY, currency)
      val fragment = InviteFriendsVerificationFragment()
      fragment.arguments = bundle
      return fragment
    }
  }
}

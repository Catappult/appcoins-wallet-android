package com.asfoundation.wallet.referrals

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.invite_friends_verification_layout.*
import java.math.BigDecimal
import javax.inject.Inject

class InviteFriendsVerificationFragment : BasePageViewFragment(), InviteFriendsVerificationView {

  @Inject
  lateinit var formatter: CurrencyFormatUtils
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
    val formattedAmount = formatter.formatCurrency(amount, WalletCurrency.FIAT)
    verification_description.text = getString(R.string.referral_view_unverified_body,
        currency.plus(formattedAmount))
  }

  override fun verifyButtonClick() = RxView.clicks(verify_button)

  override fun beenInvitedClick() = RxView.clicks(invited_button)

  override fun navigateToWalletValidation(beenInvited: Boolean) {
    activity.navigateToWalletValidation(beenInvited)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
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
      arguments!!.getString(CURRENCY)!!
    } else {
      throw IllegalArgumentException("Currency not found")
    }
  }

  companion object {

    private const val AMOUNT = "amount"
    private const val CURRENCY = "currency"

    fun newInstance(amount: BigDecimal, currency: String): InviteFriendsVerificationFragment {
      val bundle = Bundle().apply {
        putSerializable(AMOUNT, amount)
        putString(CURRENCY, currency)
      }
      return InviteFriendsVerificationFragment().apply {
        arguments = bundle
      }
    }
  }
}

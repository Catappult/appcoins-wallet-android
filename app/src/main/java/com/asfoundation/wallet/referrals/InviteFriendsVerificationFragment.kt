package com.asfoundation.wallet.referrals

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.databinding.InviteFriendsVerificationLayoutBinding
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class InviteFriendsVerificationFragment : BasePageViewFragment(), InviteFriendsVerificationView {

  @Inject
  lateinit var formatter: CurrencyFormatUtils
  private lateinit var presenter: InviteFriendsVerificationPresenter
  private lateinit var activity: InviteFriendsActivityView

  private val binding by viewBinding(InviteFriendsVerificationLayoutBinding::bind)

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
                            savedInstanceState: Bundle?): View = InviteFriendsVerificationLayoutBinding.inflate(inflater).root

  private fun setDescriptionText() {
    val formattedAmount = formatter.formatCurrency(amount, WalletCurrency.FIAT)
    binding.verificationDescription.text = getString(R.string.referral_view_unverified_body,
        currency.plus(formattedAmount))
  }

  override fun verifyButtonClick() = RxView.clicks(binding.verifyButton)

  override fun beenInvitedClick() = RxView.clicks(binding.invitedButton)

  override fun navigateToWalletValidation(beenInvited: Boolean) {
    activity.navigateToWalletValidation(beenInvited)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  val amount: BigDecimal by lazy {
    if (requireArguments().containsKey(AMOUNT)) {
      requireArguments().getSerializable(AMOUNT) as BigDecimal
    } else {
      throw IllegalArgumentException("Amount not found")
    }
  }

  val currency: String by lazy {
    if (requireArguments().containsKey(CURRENCY)) {
      requireArguments().getString(CURRENCY)!!
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

package com.asfoundation.wallet.referrals

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

  private var _binding: InviteFriendsVerificationLayoutBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  // invite_friends_verification_layout.xml
  private val verification_description get() = binding.verificationDescription
  private val verify_button get() = binding.verifyButton
  private val invited_button get() = binding.invitedButton

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
    _binding = InviteFriendsVerificationLayoutBinding.inflate(inflater, container, false)
    return binding.root
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
    _binding = null
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

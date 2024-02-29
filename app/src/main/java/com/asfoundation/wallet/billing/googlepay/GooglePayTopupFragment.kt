package com.asfoundation.wallet.billing.googlepay

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.TextDelegate
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentGooglePayTopupBinding
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.topup.TopUpActivityView
import com.asfoundation.wallet.topup.TopUpPaymentData
import com.asfoundation.wallet.topup.adyen.TopUpNavigator
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import org.apache.commons.lang3.StringUtils
import javax.inject.Inject

@AndroidEntryPoint
class GooglePayTopupFragment() : BasePageViewFragment() {

  private val viewModel: GooglePayTopupViewModel by viewModels()

  private var binding: FragmentGooglePayTopupBinding? = null
  private val views get() = binding!!
  private lateinit var compositeDisposable: CompositeDisposable
  private var topUpActivityView: TopUpActivityView? = null

  @Inject
  lateinit var navigator: TopUpNavigator

  private val TAG = GooglePayTopupFragment::class.java.name

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentGooglePayTopupBinding.inflate(inflater, container, false)
    compositeDisposable = CompositeDisposable()
    return views.root
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is TopUpActivityView) { "GooglePayTopupFragment must be attached to Topup activity" }
    topUpActivityView = context
    topUpActivityView?.lockOrientation()
  }

  override fun onResume() {
    super.onResume()
    // checks success/error/cancel
    viewModel.processGooglePayResult(amount)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setListeners()
    handleBonusAnimation()
    showLoadingAnimation()
    setObserver()
    startPayment()
  }

  private fun setObserver() {
    viewModel.state.observe(viewLifecycleOwner) { state ->
      when (state) {
        GooglePayTopupViewModel.State.Start -> {
          showLoadingAnimation()
        }

        is GooglePayTopupViewModel.State.Error -> {
          showSpecificError(state.stringRes)
        }

        is GooglePayTopupViewModel.State.SuccessPurchase -> {
          handleSuccess()
        }

        is GooglePayTopupViewModel.State.WebAuthentication -> {
          viewModel.openUrlCustomTab(requireContext(), state.url)
        }

        GooglePayTopupViewModel.State.GooglePayBack -> {
          close()
        }
      }
    }
  }

  private fun startPayment() {
    viewModel.startPayment(
      amount = amount,
      currency = currency
    )
  }

  private fun setListeners() {
    views.googlePayErrorButtons.errorBack.setOnClickListener {
      close()
    }
    views.googlePayErrorButtons.errorCancel.setOnClickListener {
      close()
    }
    views.googlePayErrorButtons.errorTryAgain.setOnClickListener {
      close()
    }
    views.googlePayErrorLayout.layoutSupportIcn.setOnClickListener {
      viewModel.showSupport(gamificationLevel)
    }
  }

  private fun handleSuccess() {
    val bundle = viewModel.createBundle(amount, currency, currencySymbol, bonus)
    navigator.popView(bundle)
  }

  private fun close() {
    navigator.navigateBack()
  }

  override fun onDetach() {
    super.onDetach()
    topUpActivityView = null
  }

  override fun onDestroyView() {
    super.onDestroyView()
    topUpActivityView?.unlockRotation()
  }

  private fun showLoadingAnimation() {
    views.successContainer.iabActivityTransactionCompleted.visibility = View.GONE
    views.loadingAuthorizationAnimation.visibility = View.VISIBLE
  }

  private fun showSpecificError(@StringRes stringRes: Int) {
    views.successContainer.iabActivityTransactionCompleted.visibility = View.GONE
    views.loadingAuthorizationAnimation.visibility = View.GONE
    val message = getString(stringRes)
    views.googlePayErrorLayout.errorMessage.text = message
    views.googlePayErrorLayout.root.visibility = View.VISIBLE
    views.googlePayErrorButtons.root.visibility = View.VISIBLE
  }

  private fun handleBonusAnimation() {
    if (StringUtils.isNotBlank(bonus)) {
      views.successContainer.lottieTransactionSuccess.setAnimation(R.raw.transaction_complete_bonus_animation_new)
      setupTransactionCompleteAnimation()
    } else {
      views.successContainer.lottieTransactionSuccess.setAnimation(R.raw.success_animation)
    }
  }

  private fun setupTransactionCompleteAnimation() {
    val textDelegate = TextDelegate(views.successContainer.lottieTransactionSuccess)
    textDelegate.setText("bonus_value", bonus)
    textDelegate.setText(
      "bonus_received",
      resources.getString(R.string.gamification_purchase_completed_bonus_received)
    )
    views.successContainer.lottieTransactionSuccess.setTextDelegate(textDelegate)
    views.successContainer.lottieTransactionSuccess.setFontAssetDelegate(object :
      FontAssetDelegate() {
      override fun fetchFont(fontFamily: String): Typeface {
        return Typeface.create("sans-serif-medium", Typeface.BOLD)
      }
    })
  }

  private val amount: String by lazy {
    if (requireArguments().containsKey(AMOUNT_KEY)) {
      requireArguments().getSerializable(AMOUNT_KEY) as String
    } else {
      throw IllegalArgumentException("amount data not found")
    }
  }

  private val currency: String by lazy {
    if (requireArguments().containsKey(CURRENCY_KEY)) {
      requireArguments().getString(CURRENCY_KEY, "")
    } else {
      throw IllegalArgumentException("currency data not found")
    }
  }

  private val currencySymbol: String by lazy {
    if (requireArguments().containsKey(CURRENCY_SYMBOL)) {
      requireArguments().getString(CURRENCY_SYMBOL, "")
    } else {
      throw IllegalArgumentException("currency data not found")
    }
  }

  private val bonus: String by lazy {
    if (requireArguments().containsKey(BONUS_KEY)) {
      requireArguments().getString(BONUS_KEY, "")
    } else {
      throw IllegalArgumentException("bonus data not found")
    }
  }

  private val gamificationLevel: Int by lazy {
    if (requireArguments().containsKey(GAMIFICATION_LEVEL)) {
      requireArguments().getInt(GAMIFICATION_LEVEL, 0)
    } else {
      throw IllegalArgumentException("gamification level data not found")
    }
  }

  companion object {

    private const val PAYMENT_TYPE_KEY = "payment_type"
    private const val AMOUNT_KEY = "amount"
    private const val CURRENCY_KEY = "currency"
    private const val CURRENCY_SYMBOL = "currency_symbol"
    private const val BONUS_KEY = "bonus"
    private const val GAMIFICATION_LEVEL = "gamification_level"

    @JvmStatic
    fun newInstance(
      paymentType: PaymentType,
      data: TopUpPaymentData,
      amount: String,
      currency: String?,
      bonus: String?,
      gamificationLevel: Int
    ): GooglePayTopupFragment = GooglePayTopupFragment().apply {
      arguments = Bundle().apply {
        putString(PAYMENT_TYPE_KEY, paymentType.name)
        putSerializable(AMOUNT_KEY, amount)
        putString(CURRENCY_KEY, currency)
        putString(CURRENCY_SYMBOL, data.fiatCurrencySymbol)
        putString(BONUS_KEY, bonus)
        putInt(GAMIFICATION_LEVEL, gamificationLevel)
      }
    }

  }

}

package com.asfoundation.wallet.billing.paypal

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.TextDelegate
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentPaypalTopupBinding
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.topup.TopUpActivityView
import com.asfoundation.wallet.topup.TopUpPaymentData
import com.asfoundation.wallet.topup.adyen.TopUpNavigator
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import org.apache.commons.lang3.StringUtils
import javax.inject.Inject

@AndroidEntryPoint
class PayPalTopupFragment() : BasePageViewFragment() {

  private val viewModel: PayPalTopupViewModel by viewModels()

  private var binding: FragmentPaypalTopupBinding? = null
  private val views get() = binding!!
  private lateinit var compositeDisposable: CompositeDisposable
  private var topUpActivityView: TopUpActivityView? = null

  private lateinit var resultAuthLauncher: ActivityResultLauncher<Intent>

  @Inject
  lateinit var navigator: TopUpNavigator

  private val TAG = PayPalTopupFragment::class.java.name

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentPaypalTopupBinding.inflate(inflater, container, false)
    compositeDisposable = CompositeDisposable()
    registerWebViewResult()
    return views.root
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is TopUpActivityView) { "Paypal topup fragment must be attached to Topup activity" }
    topUpActivityView = context
    topUpActivityView?.lockOrientation()
  }

  private fun registerWebViewResult() {
    resultAuthLauncher =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.data?.dataString?.contains(PaypalReturnSchemas.RETURN.schema) == true) {
          Log.d(this.tag, "startWebViewAuthorization SUCCESS: ${result.data ?: ""}")
          viewModel.startBillingAgreement(
            amount = amount,
            currency = currency
          )
        } else if (
          result.resultCode == Activity.RESULT_CANCELED ||
          (result.data?.dataString?.contains(PaypalReturnSchemas.CANCEL.schema) == true)
        ) {
          Log.d(this.tag, "startWebViewAuthorization CANCELED: ${result.data ?: ""}")
          viewModel.cancelToken()
        }
      }
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
        PayPalTopupViewModel.State.Start -> {
          showLoadingAnimation()
        }
        is PayPalTopupViewModel.State.Error -> {
          showSpecificError(state.stringRes)
        }
        is PayPalTopupViewModel.State.SuccessPurchase -> {
          handleSuccess()
        }
        PayPalTopupViewModel.State.TokenCanceled -> {
          close()
        }
        is PayPalTopupViewModel.State.WebViewAuthentication -> {
          startWebViewAuthorization(state.url)
        }
      }
    }
  }

  private fun startPayment() {
    viewModel.startPayment(
      createTokenIfNeeded = true,
      amount = amount,
      currency = currency
    )
  }

  private fun setListeners() {
    views.paypalErrorButtons.errorBack.setOnClickListener {
      close()
    }
    views.paypalErrorButtons.errorCancel.setOnClickListener {
      close()
    }
    views.paypalErrorButtons.errorTryAgain.setOnClickListener {
      close()
    }
    views.paypalErrorLayout.layoutSupportIcn.setOnClickListener {
      viewModel.showSupport(gamificationLevel)
    }
  }

  private fun startWebViewAuthorization(url: String) {
    val intent = WebViewActivity.newIntent(requireActivity(), url)
    resultAuthLauncher.launch(intent)
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
    views.paypalErrorLayout.errorMessage.text = message
    views.paypalErrorLayout.root.visibility = View.VISIBLE
    views.paypalErrorButtons.root.visibility = View.VISIBLE
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
    ): PayPalTopupFragment = PayPalTopupFragment().apply {
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

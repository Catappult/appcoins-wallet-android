package com.asfoundation.wallet.billing.true_layer

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.viewModels
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.TextDelegate
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentTrueLayerTopupBinding
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.topup.TopUpActivityView
import com.asfoundation.wallet.topup.TopUpPaymentData
import com.asfoundation.wallet.topup.adyen.TopUpNavigator
import com.truelayer.payments.core.domain.configuration.Environment
import com.truelayer.payments.core.domain.configuration.HttpConnectionConfiguration
import com.truelayer.payments.core.domain.configuration.HttpLoggingLevel
import com.truelayer.payments.ui.TrueLayerUI
import com.truelayer.payments.ui.screens.processor.ProcessorActivityContract
import com.truelayer.payments.ui.screens.processor.ProcessorContext
import com.truelayer.payments.ui.screens.processor.ProcessorResult
import com.truelayer.payments.ui.theme.DarkColorDefaults
import com.truelayer.payments.ui.theme.LightColorDefaults
import com.truelayer.payments.ui.theme.TrueLayerTheme
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import org.apache.commons.lang3.StringUtils
import javax.inject.Inject

@AndroidEntryPoint
class TrueLayerTopupFragment() : BasePageViewFragment() {

  private val viewModel: TrueLayerTopupViewModel by viewModels()

  private var binding: FragmentTrueLayerTopupBinding? = null
  private val views get() = binding!!
  private lateinit var compositeDisposable: CompositeDisposable
  private var topUpActivityView: TopUpActivityView? = null

  private lateinit var processorResult: ActivityResultLauncher<ProcessorContext>

  @Inject
  lateinit var navigator: TopUpNavigator

  private val TAG = TrueLayerTopupFragment::class.java.name

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentTrueLayerTopupBinding.inflate(inflater, container, false)
    compositeDisposable = CompositeDisposable()
//    iabView.disableBack()
    registerSdkResult()
    return views.root
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is TopUpActivityView) { "TrueLayerTopupFragment must be attached to Topup activity" }
    topUpActivityView = context
    topUpActivityView?.lockOrientation()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setListeners()
    handleBonusAnimation()
    showLoadingAnimation()
    setObserver()
    startPayment()
//    viewModel.handleBack(iabView.backButtonPress())
  }

  private fun setObserver() {
    viewModel.state.observe(viewLifecycleOwner) { state ->
      when (state) {
        TrueLayerTopupViewModel.State.Start -> {
          showLoadingAnimation()
        }

        is TrueLayerTopupViewModel.State.Error -> {
          showSpecificError(state.stringRes)
        }

        is TrueLayerTopupViewModel.State.SuccessPurchase -> {
          handleSuccess()
        }

        is TrueLayerTopupViewModel.State.LaunchTrueLayerSDK -> {
          launchTrueLayerSDK(state.paymentId, state.resourceToken)
        }

        TrueLayerTopupViewModel.State.TrueLayerBack -> {
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
    views.trueLayerErrorButtons.errorBack.setOnClickListener {
      close()
    }
    views.trueLayerErrorButtons.errorCancel.setOnClickListener {
      close()
    }
    views.trueLayerErrorButtons.errorTryAgain.setOnClickListener {
      close()
    }
    views.trueLayerErrorLayout.layoutSupportIcn.setOnClickListener {
      viewModel.showSupport(gamificationLevel)
    }
  }

//  private fun startWebViewAuthorization(htmlData: String) {   //TODO remove
//    val intent = WebViewActivity.newIntentFromData(requireActivity(), htmlData)
//    resultAuthLauncher.launch(intent)
//  }

//  private fun registerWebViewResult() {    //TODO remove
//    resultAuthLauncher =
//      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        when {
//          result.data?.dataString?.contains(TrueLayerReturnSchemas.SUCCESS.schema) == true -> {
//            Log.d(this.tag, "startWebViewAuthorization SUCCESS: ${result.data ?: ""}")
//            viewModel.waitForSuccess(
//              viewModel.uid,
//              amount,
//              false
//            )
//          }
//
//          result.data?.dataString?.contains(TrueLayerReturnSchemas.ERROR.schema) == true -> {
//            Log.d(this.tag, "startWebViewAuthorization ERROR: ${result.data ?: ""}")
//            viewModel._state
//              .postValue(
//                TrueLayerTopupViewModel.State.Error(
//                  R.string.purchase_error_one_wallet_generic
//                )
//              )
//          }
//
//          result.resultCode == Activity.RESULT_CANCELED -> {
//            Log.d(this.tag, "startWebViewAuthorization CANCELED: ${result.data ?: ""}")
//            close()
//          }
//
//        }
//      }
//  }

  private fun handleSuccess(pendingFinalConfirmation: Boolean = true) {
    val bundle = viewModel.createBundle(amount, currency, currencySymbol, bonus, pendingFinalConfirmation)
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
    views.trueLayerErrorLayout.errorMessage.text = message
    views.trueLayerErrorLayout.root.visibility = View.VISIBLE
    views.trueLayerErrorButtons.root.visibility = View.VISIBLE
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

  private fun launchTrueLayerSDK(paymentId: String, resourceToken: String) {
    TrueLayerUI.init(context = views.root.context) {
      // optionally choose which environment you want to use: PRODUCTION or SANDBOX
      environment = if (BuildConfig.DEBUG) Environment.SANDBOX else Environment.PRODUCTION
      // Make your own custom http configuration, stating custom timeout and http request logging level
      httpConnection = HttpConnectionConfiguration(
        timeoutMs = 15000,
        httpDebugLoggingLevel = HttpLoggingLevel.None
      )
    }

    val paymentContext = ProcessorContext.PaymentContext(
      id = paymentId,
      resourceToken = resourceToken,
      redirectUri = "appcoins://truelayer",
    )

    processorResult.launch(paymentContext)

  }

  private fun registerSdkResult() {
    val contract = ProcessorActivityContract()
    processorResult = registerForActivityResult(contract) {
      when (it) {
        is ProcessorResult.Failure -> {
          when (it.reason) {
            ProcessorResult.FailureReason.UserAborted,
            ProcessorResult.FailureReason.UserAbortedFailedToNotifyBackend,
            ProcessorResult.FailureReason.UserAbortedProviderTemporarilyUnavailable,
            ProcessorResult.FailureReason.UserAbortedProviderTemporarilyUnavailableFailedToNotifyBackend,
            -> {
              Log.d(TAG, "TrueLayer Back: ${it.reason.name}")
              navigator.navigateBack()
            }

            else -> {
              Log.d(TAG, "TrueLayer Fail: ${it.reason.name}")
              viewModel.sendErrorEvent(it.reason.name)
              showSpecificError(R.string.purchase_error_open_banking_wallet_generic)
            }
          }


          Log.d(TAG, "TrueLayer Fail: ${it.reason.name}")
          viewModel.sendErrorEvent(it.reason.name)
          showSpecificError(R.string.purchase_error_open_banking_wallet_generic)
        }

        is ProcessorResult.Successful -> {
          // it.step: Redirect, Wait, Authorized, Successful, Settled
          Log.d(TAG, "TrueLayer Success: ${it.step.name}")
          when (it.step) {
            ProcessorResult.PaymentStep.Successful,
            ProcessorResult.PaymentStep.Authorized,
            ProcessorResult.PaymentStep.Settled,
            -> {
              handleSuccess()
            }

            ProcessorResult.PaymentStep.Redirect -> {
              handleSuccess()  // TODO check if needs a new screen
            }

            ProcessorResult.PaymentStep.Wait -> {}

          }
        }
      }
    }
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
    ): TrueLayerTopupFragment = TrueLayerTopupFragment().apply {
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
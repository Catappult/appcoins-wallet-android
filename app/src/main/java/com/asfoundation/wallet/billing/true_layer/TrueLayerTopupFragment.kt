package com.asfoundation.wallet.billing.true_layer

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.compose.runtime.rememberCoroutineScope
import androidx.fragment.app.viewModels
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.TextDelegate
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentTrueLayerTopupBinding
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.topup.TopUpActivity
import com.asfoundation.wallet.topup.TopUpActivityView
import com.asfoundation.wallet.topup.TopUpPaymentData
import com.asfoundation.wallet.topup.adyen.TopUpNavigator
import com.truelayer.payments.core.domain.configuration.Environment
import com.truelayer.payments.core.domain.configuration.HttpConnectionConfiguration
import com.truelayer.payments.core.domain.configuration.HttpLoggingLevel
import com.truelayer.payments.ui.TrueLayerUI
import com.truelayer.payments.ui.screens.processor.Processor
import com.truelayer.payments.ui.screens.processor.ProcessorContext
import com.truelayer.payments.ui.screens.processor.ProcessorResult
import com.truelayer.payments.ui.theme.DarkColorDefaults
import com.truelayer.payments.ui.theme.Theme
import com.truelayer.payments.ui.theme.TrueLayerTheme
import com.truelayer.payments.ui.theme.TypographyDefaults
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import javax.inject.Inject


@AndroidEntryPoint
class TrueLayerTopupFragment() : BasePageViewFragment() {

  private val viewModel: TrueLayerTopupViewModel by viewModels()

  private var binding: FragmentTrueLayerTopupBinding? = null
  private val views get() = binding!!
  private lateinit var compositeDisposable: CompositeDisposable
  private var topUpActivityView: TopUpActivityView? = null

  @Inject
  lateinit var navigator: TopUpNavigator

  private val TAG = TrueLayerTopupFragment::class.java.name

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentTrueLayerTopupBinding.inflate(inflater, container, false)
    compositeDisposable = CompositeDisposable()
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
    setObserver()
    startPayment()
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
          hideLoadingAnimation()
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
      viewModel.showSupport()
    }
  }

  private fun handleSuccess(pendingFinalConfirmation: Boolean = true) {
    val bundle =
      viewModel.createBundle(amount, currency, currencySymbol, bonus, pendingFinalConfirmation)
    navigator.popView(bundle)
  }

  private fun close() {
    navigator.navigateBack()
    topUpActivityView?.getFullscreenComposeView()?.setContent { }  // resets true layer view
    requireActivity().finish()
    val intentTopup =
      TopUpActivity.newIntent(requireContext()).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
      }
    requireContext().startActivity(intentTopup)
  }

  override fun onDetach() {
    super.onDetach()
    topUpActivityView = null
  }

  override fun onDestroyView() {
    super.onDestroyView()
    topUpActivityView?.unlockRotation()
  }

  private fun hideLoadingAnimation() {
    views.loadingAuthorizationAnimation.visibility = View.GONE
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
      environment = if (BuildConfig.DEBUG) Environment.SANDBOX else Environment.PRODUCTION
      httpConnection = HttpConnectionConfiguration(
        timeoutMs = 15000,
        httpDebugLoggingLevel = if (BuildConfig.DEBUG) HttpLoggingLevel.Body else HttpLoggingLevel.None
      )
    }

    val universalColorPalette = DarkColorDefaults.copy(
      primary = WalletColors.styleguide_pink,
      onPrimary = WalletColors.styleguide_white,
      secondary = WalletColors.styleguide_light_grey,
      background = WalletColors.styleguide_blue,
      surface = WalletColors.styleguide_blue,
      onSurface = WalletColors.styleguide_white,
      surfaceVariant = WalletColors.styleguide_blue,
      onSurfaceVariant = WalletColors.styleguide_white,
    )

    val theme = TrueLayerTheme(
      lightPalette = universalColorPalette,
      darkPalette = universalColorPalette,
      typography = TypographyDefaults
    )

    val paymentContext = ProcessorContext.PaymentContext(
      id = paymentId,
      resourceToken = resourceToken,
      redirectUri = "appcoins://truelayer"
    )

    topUpActivityView?.getFullscreenComposeView()?.visibility = View.VISIBLE
    topUpActivityView?.getFullscreenComposeView()?.setContent {
      val coroutineScope = rememberCoroutineScope()

      Theme(
        theme = theme
      ) {
        Processor(
          context = paymentContext,
          onSuccess = { successStep ->
            // it.step: Redirect, Wait, Authorized, Successful, Settled
            Log.d(TAG, "TrueLayer Success: ${successStep.step.name}")
            when (successStep.step) {
              ProcessorResult.PaymentStep.Successful,
              ProcessorResult.PaymentStep.Authorized,
              ProcessorResult.PaymentStep.Settled,
              -> {
                handleSuccess()
              }

              ProcessorResult.PaymentStep.Redirect -> {
                coroutineScope.launch {
                  delay(500L)
                  handleSuccess()
                }
              }

              ProcessorResult.PaymentStep.Wait -> {}

            }
          },
          onFailure = { failureReason ->
            when (failureReason.reason) {
              ProcessorResult.FailureReason.UserAborted,
              ProcessorResult.FailureReason.UserAbortedFailedToNotifyBackend,
              ProcessorResult.FailureReason.UserAbortedProviderTemporarilyUnavailable,
              ProcessorResult.FailureReason.UserAbortedProviderTemporarilyUnavailableFailedToNotifyBackend,
              -> {
                Log.d(TAG, "TrueLayer Back: ${failureReason.reason.name}")
                close()
              }

              else -> {
                Log.d(TAG, "TrueLayer Fail: ${failureReason.reason.name}")
                viewModel.sendErrorEvent(failureReason.reason.name)
                showSpecificError(R.string.purchase_error_open_banking_wallet_generic)
              }
            }
          },
        )
      }
    }
  }

  private val amount by lazy {
    if (requireArguments().containsKey(AMOUNT_KEY)) {
      requireArguments().getString(AMOUNT_KEY)!!
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
        putString(AMOUNT_KEY, amount)
        putString(CURRENCY_KEY, currency)
        putString(CURRENCY_SYMBOL, data.fiatCurrencySymbol)
        putString(BONUS_KEY, bonus)
        putInt(GAMIFICATION_LEVEL, gamificationLevel)
      }
    }

  }

}
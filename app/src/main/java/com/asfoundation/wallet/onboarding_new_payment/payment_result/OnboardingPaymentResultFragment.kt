package com.asfoundation.wallet.onboarding_new_payment.payment_result

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StringRes
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.billing.ErrorInfo
import com.appcoins.wallet.billing.util.Error
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asf.wallet.R
import com.asf.wallet.databinding.OnboardingPaymentResultFragmentBinding
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.onboarding_new_payment.getPurchaseBonusMessage
import com.asfoundation.wallet.onboarding_new_payment.payment_result.SdkPaymentWebSocketListener.Companion.SDK_STATUS_FATAL_ERROR
import com.asfoundation.wallet.onboarding_new_payment.payment_result.SdkPaymentWebSocketListener.Companion.SDK_STATUS_INVALID_ARGUMENTS
import com.asfoundation.wallet.onboarding_new_payment.payment_result.SdkPaymentWebSocketListener.Companion.SDK_STATUS_ITEM_ALREADY_OWNED
import com.asfoundation.wallet.onboarding_new_payment.payment_result.SdkPaymentWebSocketListener.Companion.SDK_STATUS_NETWORK_DOWN
import com.asfoundation.wallet.onboarding_new_payment.payment_result.SdkPaymentWebSocketListener.Companion.SDK_STATUS_SUCCESS
import com.asfoundation.wallet.onboarding_new_payment.payment_result.SdkPaymentWebSocketListener.Companion.SDK_STATUS_USER_CANCEL
import com.asfoundation.wallet.service.ServicesErrorCodeMapper
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.lang3.StringUtils
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingPaymentResultFragment : BasePageViewFragment(),
  SingleStateFragment<OnboardingPaymentResultState, OnboardingPaymentResultSideEffect> {

  private val viewModel: OnboardingPaymentResultViewModel by viewModels()
  private val sharedViewModel: OnboardingSharedHeaderViewModel by activityViewModels()
  private val views by viewBinding(OnboardingPaymentResultFragmentBinding::bind)
  lateinit var args: OnboardingPaymentResultFragmentArgs
  private lateinit var outerNavController: NavController
  private val clientWebSocket = OkHttpClient()


  @Inject
  lateinit var servicesErrorCodeMapper: ServicesErrorCodeMapper

  @Inject
  lateinit var adyenErrorCodeMapper: AdyenErrorCodeMapper

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var navigator: OnboardingPaymentResultNavigator

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return OnboardingPaymentResultFragmentBinding.inflate(inflater).root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initOuterNavController()
    args = OnboardingPaymentResultFragmentArgs.fromBundle(requireArguments())
    views.loadingAnimation.playAnimation()
    clickListeners()
    // To hide the header inside other fragment OnboardingPaymentFragment
    sharedViewModel.viewVisibility.value = View.GONE
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onDestroy() {
    super.onDestroy()
    // Shut down the dispatcher when it's no longer needed
    clientWebSocket.dispatcher.executorService.shutdown()
  }

  private fun clickListeners() {
    //try again and back needs to be separated later
    views.genericErrorButton.setOnClickListener {
      sharedViewModel.viewVisibility.value = View.VISIBLE
      navigator.navigateBackToPaymentMethods()
      viewModel.setResponseCodeWebSocket(SDK_STATUS_USER_CANCEL)
    }
    views.genericErrorLayout.layoutSupportIcn.setOnClickListener {
      viewModel.showSupport(args.forecastBonus.level)
    }
    views.genericErrorLayout.layoutSupportLogo.setOnClickListener {
      viewModel.showSupport(args.forecastBonus.level)
    }
    views.onboardingSuccessButtons.backToGameButton.setOnClickListener {
      viewModel.handleBackToGameClick()
    }
    views.onboardingSuccessButtons.exploreWalletButton.setOnClickListener {
      viewModel.handleExploreWalletClick()
    }
    requireActivity().onBackPressedDispatcher.addCallback(
      viewLifecycleOwner,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          if (viewModel.getResponseCodeWebSocket() != SDK_STATUS_SUCCESS) {
            createWebSocketSdk()
          }
          isEnabled = false
          requireActivity().onBackPressed()
        }
      })
  }

  override fun onStateChanged(state: OnboardingPaymentResultState) = Unit

  override fun onSideEffect(sideEffect: OnboardingPaymentResultSideEffect) {
    when (sideEffect) {
      is OnboardingPaymentResultSideEffect.ShowPaymentError -> {
        handleError(
          sideEffect.error,
          sideEffect.refusalCode,
          sideEffect.isWalletVerified,
          sideEffect.paymentType
        )
      }

      is OnboardingPaymentResultSideEffect.ShowPaymentSuccess -> handleSuccess()
      is OnboardingPaymentResultSideEffect.NavigateBackToGame -> navigator.navigateBackToGame(
        sideEffect.appPackageName
      )

      OnboardingPaymentResultSideEffect.NavigateToExploreWallet -> navigator.navigateToHome()
      OnboardingPaymentResultSideEffect.NavigateBackToPaymentMethods -> navigator.navigateBackToPaymentMethods()
    }
  }

  fun handleError(
    error: Error?,
    refusalCode: Int?,
    walletVerified: Boolean?,
    paymentType: PaymentType
  ) {
    when {
      error?.isNetworkError == true -> {
        showNoNetworkError()
        viewModel.setResponseCodeWebSocket(SDK_STATUS_NETWORK_DOWN)
      }

      error?.errorInfo != null -> {
        when {
          error.errorInfo?.errorType == ErrorInfo.ErrorType.INVALID_CARD -> {
            showSpecificError(R.string.purchase_error_invalid_credit_card)
            viewModel.setResponseCodeWebSocket(SDK_STATUS_INVALID_ARGUMENTS)
          }

          error.errorInfo?.errorType == ErrorInfo.ErrorType.CARD_SECURITY_VALIDATION -> {
            showSpecificError(R.string.purchase_error_card_security_validation)
            viewModel.setResponseCodeWebSocket(SDK_STATUS_INVALID_ARGUMENTS)
          }

          error.errorInfo?.errorType == ErrorInfo.ErrorType.OUTDATED_CARD -> {
            showSpecificError(R.string.purchase_card_error_re_insert)
            viewModel.setResponseCodeWebSocket(SDK_STATUS_INVALID_ARGUMENTS)
          }

          error.errorInfo?.errorType == ErrorInfo.ErrorType.ALREADY_PROCESSED -> {
            showSpecificError(R.string.purchase_error_card_already_in_progress)
            viewModel.setResponseCodeWebSocket(SDK_STATUS_ITEM_ALREADY_OWNED)
          }

          error.errorInfo?.errorType == ErrorInfo.ErrorType.PAYMENT_ERROR -> {
            showSpecificError(R.string.purchase_error_payment_rejected)
            viewModel.setResponseCodeWebSocket(SDK_STATUS_FATAL_ERROR)
          }

          error.errorInfo?.errorType == ErrorInfo.ErrorType.INVALID_COUNTRY_CODE -> {
            showSpecificError(R.string.unknown_error)
            viewModel.setResponseCodeWebSocket(SDK_STATUS_INVALID_ARGUMENTS)
          }

          error.errorInfo?.errorType == ErrorInfo.ErrorType.PAYMENT_NOT_SUPPORTED_ON_COUNTRY -> {
            showSpecificError(R.string.purchase_error_payment_rejected)
            viewModel.setResponseCodeWebSocket(SDK_STATUS_INVALID_ARGUMENTS)
          }

          error.errorInfo?.errorType == ErrorInfo.ErrorType.CURRENCY_NOT_SUPPORTED -> {
            showSpecificError(R.string.purchase_card_error_general_1)
            viewModel.setResponseCodeWebSocket(SDK_STATUS_INVALID_ARGUMENTS)
          }

          error.errorInfo?.errorType == ErrorInfo.ErrorType.TRANSACTION_AMOUNT_EXCEEDED -> {
            showSpecificError(R.string.purchase_card_error_no_funds)
            viewModel.setResponseCodeWebSocket(SDK_STATUS_INVALID_ARGUMENTS)
          }

          error.errorInfo?.httpCode != null -> {
            val resId = servicesErrorCodeMapper.mapError(error.errorInfo?.errorType)
            if (error.errorInfo?.httpCode == HTTP_FRAUD_CODE) viewModel.handleFraudFlow(
              error,
              AdyenErrorCodeMapper.FRAUD
            )
            else showSpecificError(resId)
          }

          else -> {
            showSpecificError(R.string.unknown_error)
            viewModel.setResponseCodeWebSocket(SDK_STATUS_FATAL_ERROR)
          }
        }
      }

      walletVerified != null -> {
        /*
        * Wallet or card verification flow should be addressed here, but the user can't complete the
        * the verification flow without leaving the first payment flow
        * */
        views.genericErrorLayout.errorVerifyWalletButton.visibility = View.VISIBLE
        showSpecificError(R.string.purchase_error_verify_wallet)
        views.genericErrorLayout.errorVerifyWalletButton.setOnClickListener {
          navigateToVerifyPaymentMethod(walletVerified, paymentType)
        }
      }

      refusalCode != null -> {
        viewModel.setResponseCodeWebSocket(SDK_STATUS_FATAL_ERROR)
        showSpecificError(adyenErrorCodeMapper.map(refusalCode))
      }

      else -> {
        viewModel.setResponseCodeWebSocket(SDK_STATUS_FATAL_ERROR)
        showSpecificError(R.string.unknown_error)
      }
    }
  }

  fun showSpecificError(@StringRes errorMessageRes: Int) {
    views.loadingAnimation.visibility = View.GONE
    views.genericErrorLayout.root.visibility = View.VISIBLE
    views.genericErrorButton.visibility = View.VISIBLE
    views.genericErrorLayout.errorMessage.text = getString(errorMessageRes)
  }

  fun showNoNetworkError() {
    views.loadingAnimation.visibility = View.GONE
    views.genericErrorLayout.root.visibility = View.GONE
    views.genericErrorButton.visibility = View.GONE
    views.noNetworkErrorLayout.root.visibility = View.VISIBLE
  }

  private fun handleSuccess() {
    views.loadingAnimation.visibility = View.GONE
    views.genericErrorLayout.root.visibility = View.GONE
    views.genericErrorButton.visibility = View.GONE
    handleBonusAnimation()
    views.onboardingGenericSuccessLayout.root.visibility = View.VISIBLE
    views.onboardingGenericSuccessLayout.onboardingActivityTransactionCompleted.visibility =
      View.VISIBLE
    views.onboardingSuccessButtons.root.visibility = View.VISIBLE
    viewModel.setResponseCodeWebSocket(SDK_STATUS_SUCCESS)
    createWebSocketSdk()
  }

  private fun handleBonusAnimation() {
    val purchaseBonusMessage = args.forecastBonus.getPurchaseBonusMessage(formatter)
    if (StringUtils.isNotBlank(purchaseBonusMessage)) {
      views.onboardingGenericSuccessLayout.onboardingBonusSuccessLayout.visibility = View.VISIBLE
      views.onboardingGenericSuccessLayout.onboardingTransactionSuccessBonusText.text =
        String.format(getString(R.string.bonus_granted_body), purchaseBonusMessage)
    }
  }

  private fun navigateToVerifyPaymentMethod(
    walletVerified: Boolean,
    paymentMethodType: PaymentType
  ) {
    if (paymentMethodType == PaymentType.PAYPAL) navigator.navigateToVerifyPayPal(outerNavController)
    else navigator.navigateToVerifyCreditCard(walletVerified)
  }

  private fun initOuterNavController() {
    outerNavController = Navigation.findNavController(requireActivity(), R.id.main_host_container)
  }

  private fun createWebSocketSdk() {
    if (args.transactionBuilder.type == "INAPP") {
      if (args.transactionBuilder.wspPort == null) {
        val responseCode = viewModel.getResponseCodeWebSocket()
        val productToken = args.paymentModel.purchaseUid
        val purchaseResultJson = """{"responseCode": $responseCode, "purchaseToken": "$productToken"}"""
        val encodedPurchaseResult = Uri.encode(purchaseResultJson)

        val deepLinkUri = Uri.Builder()
          .scheme("web-iap-result")
          .authority(args.transactionBuilder.domain)
          .appendQueryParameter("purchaseResult", encodedPurchaseResult)
          .build()

        val deepLinkIntent = Intent(Intent.ACTION_VIEW, deepLinkUri)

        deepLinkIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        Handler(Looper.getMainLooper()).postDelayed({
          startActivity(deepLinkIntent)
        }, 1600)

      } else {
        val request =
          Request.Builder().url("ws://localhost:".plus(args.transactionBuilder.wspPort)).build()
        val listener = SdkPaymentWebSocketListener(
          args.paymentModel.purchaseUid,
          args.paymentModel.uid,
          viewModel.getResponseCodeWebSocket()
        )
        clientWebSocket.newWebSocket(request, listener)
      }
    }
  }

  companion object {
    private const val HTTP_FRAUD_CODE = 403
  }
}
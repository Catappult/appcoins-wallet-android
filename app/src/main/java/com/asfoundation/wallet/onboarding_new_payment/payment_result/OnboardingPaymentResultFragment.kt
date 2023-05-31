package com.asfoundation.wallet.onboarding_new_payment.payment_result

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.TextDelegate
import com.appcoins.wallet.billing.ErrorInfo
import com.appcoins.wallet.billing.util.Error
import com.asf.wallet.R
import com.asf.wallet.databinding.OnboardingPaymentResultFragmentBinding
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper
import com.asfoundation.wallet.onboarding_new_payment.getPurchaseBonusMessage
import com.asfoundation.wallet.service.ServicesErrorCodeMapper
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import org.apache.commons.lang3.StringUtils
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingPaymentResultFragment : BasePageViewFragment(),
  SingleStateFragment<OnboardingPaymentResultState, OnboardingPaymentResultSideEffect> {

  private val viewModel: OnboardingPaymentResultViewModel by viewModels()
  private val views by viewBinding(OnboardingPaymentResultFragmentBinding::bind)
  lateinit var args: OnboardingPaymentResultFragmentArgs

  @Inject
  lateinit var servicesErrorCodeMapper: ServicesErrorCodeMapper

  @Inject
  lateinit var adyenErrorCodeMapper: AdyenErrorCodeMapper

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var navigator: OnboardingPaymentResultNavigator

  override fun onCreateView(
    inflater: LayoutInflater, @Nullable container: ViewGroup?,
    @Nullable savedInstanceState: Bundle?
  ): View {
    return OnboardingPaymentResultFragmentBinding.inflate(inflater).root
  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    args = OnboardingPaymentResultFragmentArgs.fromBundle(requireArguments())
    views.loadingAnimation.playAnimation()
    clickListeners()
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  private fun clickListeners() {
    //try again and back needs to be separated later
    views.genericErrorButtons.errorTryAgain.setOnClickListener {
      navigator.navigateBackToPaymentMethods()
    }
    views.genericErrorButtons.errorCancel.setOnClickListener {
      viewModel.handleExploreWalletClick()
    }
    views.genericErrorLayout.layoutSupportIcn.setOnClickListener {
      viewModel.showSupport(args.forecastBonus.level)
    }
    views.successButtons.backToGameButton.setOnClickListener {
      viewModel.handleBackToGameClick()
    }
    views.successButtons.exploreWalletButton.setOnClickListener {
      viewModel.handleExploreWalletClick()
    }
  }

  override fun onStateChanged(state: OnboardingPaymentResultState) = Unit

  override fun onSideEffect(sideEffect: OnboardingPaymentResultSideEffect) {
    when (sideEffect) {
      is OnboardingPaymentResultSideEffect.ShowPaymentError -> {
        handleError(
          sideEffect.error,
          sideEffect.refusalCode,
          sideEffect.isWalletVerified
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

  fun handleError(error: Error?, refusalCode: Int?, walletVerified: Boolean?) {
    when {
      error?.isNetworkError == true -> {
        showSpecificError(R.string.notification_no_network_poa)
      }
      error?.errorInfo != null -> {
        when {
          error.errorInfo?.errorType == ErrorInfo.ErrorType.INVALID_CARD -> {
            showSpecificError(R.string.purchase_error_invalid_credit_card)
          }
          error.errorInfo?.errorType == ErrorInfo.ErrorType.CARD_SECURITY_VALIDATION -> {
            showSpecificError(R.string.purchase_error_card_security_validation)
          }
          error.errorInfo?.errorType == ErrorInfo.ErrorType.OUTDATED_CARD -> {
            showSpecificError(R.string.purchase_card_error_re_insert)
          }
          error.errorInfo?.errorType == ErrorInfo.ErrorType.ALREADY_PROCESSED -> {
            showSpecificError(R.string.purchase_error_card_already_in_progress)
          }
          error.errorInfo?.errorType == ErrorInfo.ErrorType.PAYMENT_ERROR -> {
            showSpecificError(R.string.purchase_error_payment_rejected)
          }
          error.errorInfo?.errorType == ErrorInfo.ErrorType.INVALID_COUNTRY_CODE -> {
            showSpecificError(R.string.unknown_error)
          }
          error.errorInfo?.errorType == ErrorInfo.ErrorType.PAYMENT_NOT_SUPPORTED_ON_COUNTRY -> {
            showSpecificError(R.string.purchase_error_payment_rejected)
          }
          error.errorInfo?.errorType == ErrorInfo.ErrorType.CURRENCY_NOT_SUPPORTED -> {
            showSpecificError(R.string.purchase_card_error_general_1)
          }
          error.errorInfo?.errorType == ErrorInfo.ErrorType.TRANSACTION_AMOUNT_EXCEEDED -> {
            showSpecificError(R.string.purchase_card_error_no_funds)
          }
          error.errorInfo?.httpCode != null -> {
            showSpecificError(servicesErrorCodeMapper.mapError(error.errorInfo?.errorType))
          }
          else -> {
            showSpecificError(R.string.unknown_error)
          }
        }
      }
      walletVerified != null -> {
        /*
        * Wallet or card verification flow should be addressed here, but the user can't complete the
        * the verification flow without leaving the first payment flow
        * */
        if (walletVerified) {
          showSpecificError(R.string.purchase_error_verify_card)
        } else {
          showSpecificError(R.string.purchase_error_verify_wallet)
        }
      }
      refusalCode != null -> {
        showSpecificError(adyenErrorCodeMapper.map(refusalCode))
      }
      else -> {
        showSpecificError(R.string.unknown_error)
      }
    }
  }

  fun showSpecificError(@StringRes errorMessageRes: Int) {
    views.loadingAnimation.visibility = View.GONE
    views.genericErrorLayout.root.visibility = View.VISIBLE
    views.genericErrorButtons.root.visibility = View.VISIBLE
    views.genericErrorLayout.errorMessage.text = getString(errorMessageRes)
  }

  private fun handleSuccess() {
    views.loadingAnimation.visibility = View.GONE
    views.genericErrorLayout.root.visibility = View.GONE
    views.genericErrorButtons.root.visibility = View.GONE
    views.genericSuccessLayout.root.visibility = View.VISIBLE
    views.successButtons.root.visibility = View.VISIBLE
    handleBonusAnimation()
  }

  private fun handleBonusAnimation() {
    val purchaseBonusMessage = args.forecastBonus.getPurchaseBonusMessage(formatter)
    if (StringUtils.isNotBlank(purchaseBonusMessage)) {
      views.genericSuccessLayout.lottieTransactionSuccess.setAnimation(R.raw.transaction_complete_bonus_animation)
      setupTransactionCompleteAnimation(purchaseBonusMessage)
    } else {
      views.genericSuccessLayout.lottieTransactionSuccess.setAnimation(R.raw.success_animation)
    }
    views.genericSuccessLayout.lottieTransactionSuccess.playAnimation()
  }

  private fun setupTransactionCompleteAnimation(purchaseBonusMessage: String) {
    val textDelegate = TextDelegate(views.genericSuccessLayout.lottieTransactionSuccess)
    textDelegate.setText("bonus_value", purchaseBonusMessage)
    textDelegate.setText(
      "bonus_received",
      resources.getString(R.string.gamification_purchase_completed_bonus_received)
    )
    views.genericSuccessLayout.lottieTransactionSuccess.setTextDelegate(textDelegate)
    views.genericSuccessLayout.lottieTransactionSuccess.setFontAssetDelegate(object :
      FontAssetDelegate() {
      override fun fetchFont(fontFamily: String): Typeface {
        return Typeface.create("sans-serif-medium", Typeface.BOLD)
      }
    })
  }
}
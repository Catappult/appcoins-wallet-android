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
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper
import com.asfoundation.wallet.service.ServicesErrorCodeMapper
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import org.apache.commons.lang3.StringUtils
import java.math.BigDecimal
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
    views.genericErrorButtons.errorBack.setOnClickListener {
      navigator.navigateBackToPaymentMethods()
    }
    views.genericErrorButtons.errorCancel.setOnClickListener {
      navigator.navigateToHome()
    }
    views.genericErrorLayout.layoutSupportIcn.setOnClickListener {
      viewModel.showSupport(args.forecastBonus.level)
    }
    views.successButtons.backToGameButton.setOnClickListener {
      viewModel.handleBackToGameClick()
    }
    views.successButtons.exploreWalletButton.setOnClickListener {
      navigator.navigateToHome()
    }
  }

  override fun onStateChanged(state: OnboardingPaymentResultState) = Unit

  override fun onSideEffect(sideEffect: OnboardingPaymentResultSideEffect) {
    when (sideEffect) {
      is OnboardingPaymentResultSideEffect.ShowPaymentError -> handleError(
        sideEffect.error,
        sideEffect.refusalCode,
        sideEffect.isWalletVerified
      )
      is OnboardingPaymentResultSideEffect.ShowPaymentSuccess -> handleSuccess()
      is OnboardingPaymentResultSideEffect.NavigateBackToGame -> navigator.navigateBackToGame(
        sideEffect.appPackageName
      )
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
          error.errorInfo?.httpCode != null -> {
            showSpecificError(servicesErrorCodeMapper.mapError(error.errorInfo?.errorType))
          }
          else -> {
            showSpecificError(R.string.unknown_error)
          }
        }
      }
      walletVerified != null -> {
        if (walletVerified) {
          views.genericErrorLayout.errorVerifyWalletButton.visibility = View.GONE
          views.genericErrorLayout.errorVerifyCardButton.visibility = View.VISIBLE
          showSpecificError(R.string.purchase_error_verify_card)
        } else {
          views.genericErrorLayout.errorVerifyWalletButton.visibility = View.VISIBLE
          views.genericErrorLayout.errorVerifyCardButton.visibility = View.GONE
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
    if (StringUtils.isNotBlank(getPurchaseBonusMessage())) {
      views.genericSuccessLayout.lottieTransactionSuccess.setAnimation(R.raw.transaction_complete_bonus_animation)
      setupTransactionCompleteAnimation()
    } else {
      views.genericSuccessLayout.lottieTransactionSuccess.setAnimation(R.raw.success_animation)
    }
    views.genericSuccessLayout.lottieTransactionSuccess.playAnimation()
  }

  private fun getPurchaseBonusMessage(): String {
    var scaledBonus = args.forecastBonus.amount.stripTrailingZeros()
      .setScale(CurrencyFormatUtils.FIAT_SCALE, BigDecimal.ROUND_DOWN)
    val newCurrencyString =
      if (scaledBonus < BigDecimal("0.01")) "~${args.forecastBonus.currency}" else args.forecastBonus.currency
    scaledBonus = scaledBonus.max(BigDecimal("0.01"))
    val formattedBonus = formatter.formatCurrency(scaledBonus, WalletCurrency.FIAT)
    return newCurrencyString + formattedBonus
  }

  private fun setupTransactionCompleteAnimation() {
    val textDelegate = TextDelegate(views.genericSuccessLayout.lottieTransactionSuccess)
    textDelegate.setText("bonus_value", getPurchaseBonusMessage())
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
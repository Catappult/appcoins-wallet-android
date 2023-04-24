package com.asfoundation.wallet.onboarding_new_payment.local_payments

import androidx.activity.result.ActivityResult

import androidx.lifecycle.SavedStateHandle
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.appcoins.wallet.ui.arch.data.Async
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetPaymentLinkUseCase
import com.asfoundation.wallet.ui.iab.WebViewActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


sealed class OnboardingLocalPaymentSideEffect : SideEffect {
    data class NavigateToWebView(val uri: String) : OnboardingLocalPaymentSideEffect()
    object NavigateBackToPaymentMethods : OnboardingLocalPaymentSideEffect()
    object ShowLoading : OnboardingLocalPaymentSideEffect()
    object ShowError : OnboardingLocalPaymentSideEffect()
    object ShowSuccess : OnboardingLocalPaymentSideEffect()

    object ShowCompletablePayment : OnboardingLocalPaymentSideEffect()
}


data class OnboardingLocalPaymentState(val paymentInfoModel: Async<PaymentInfoModel> = Async.Uninitialized, val urlString: Async<String> = Async.Uninitialized) :
    ViewState

@HiltViewModel
class OnboardingLocalPaymentViewModel @Inject constructor(
    private val getPaymentLinkUseCase: GetPaymentLinkUseCase,
    private val events: OnboardingPaymentEvents,
    savedStateHandle: SavedStateHandle
) :
    BaseViewModel<OnboardingLocalPaymentState, OnboardingLocalPaymentSideEffect>(
        OnboardingLocalPaymentState()
    ) {

    private var args: OnboardingLocalPaymentFragmentArgs =
        OnboardingLocalPaymentFragmentArgs.fromSavedStateHandle(savedStateHandle)

    init {
        getPaymentLink()
    }

    fun handleWebViewResult(result: ActivityResult) {
        when (result.resultCode) {
            WebViewActivity.FAIL -> {
                sendSideEffect { OnboardingLocalPaymentSideEffect.ShowError }

            }
            WebViewActivity.SUCCESS -> {
                events.sendPaymentConclusionEvents(
                    BuildConfig.APPLICATION_ID,
                    args.transactionBuilder.skuId,
                    args.transactionBuilder.amount(),
                    args.transactionBuilder.type,
                    args.transactionBuilder.chainId.toString()
                )
                sendSideEffect { OnboardingLocalPaymentSideEffect.ShowSuccess }
            }
            WebViewActivity.USER_CANCEL -> {
                events.sendPayPalConfirmationEvent(args.transactionBuilder, "cancel")
                sendSideEffect { OnboardingLocalPaymentSideEffect.ShowError }
            }
        }
    }

    private fun getPaymentLink() {
                getPaymentLinkUseCase(
            data = args.transactionBuilder,
            currency = args.currency,
            packageName = args.transactionBuilder.domain,
            amount = args.amount,
            paymentType = args.paymentType
        ).asAsyncToState {
            args.transactionBuilder.let { transactionBuilder ->
                events.sendLocalNavigationToUrlEvents(
                    BuildConfig.APPLICATION_ID,
                    transactionBuilder.skuId,
                    transactionBuilder.amount().toString(),
                    transactionBuilder.type,
                    transactionBuilder.chainId.toString()
                )
            }
            copy(urlString = it)

        }.scopedSubscribe()
    }

    fun handleBackButton() {
        sendSideEffect { OnboardingLocalPaymentSideEffect.NavigateBackToPaymentMethods }
    }

}
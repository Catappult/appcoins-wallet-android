package com.asfoundation.wallet.onboarding_new_payment.local_payments

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import androidx.lifecycle.SavedStateHandle
import com.appcoins.wallet.appcoins.rewards.ErrorInfo
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.appcoins.wallet.ui.arch.Async
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents
import com.asfoundation.wallet.onboarding_new_payment.payment_result.OnboardingPaymentResultSideEffect
import com.asfoundation.wallet.onboarding_new_payment.payment_result.OnboardingPaymentResultState
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetPaymentLinkUseCase
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetTopUpPaymentLinkUseCase
import com.asfoundation.wallet.ui.iab.PaymentMethod
import com.asfoundation.wallet.ui.iab.localpayments.LocalPaymentPresenter
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject


sealed class OnboardingLocalPaymentSideEffect : SideEffect {
    data class NavigateToLink(val uri: Uri) : OnboardingLocalPaymentSideEffect()
    data class NavigateBackToGame(val appPackageName: String) : OnboardingLocalPaymentSideEffect()
}


object OnboardingLocalPaymentState : ViewState

@HiltViewModel
class OnboardingLocalPaymentViewModel @Inject constructor(
    private val getPaymentLinkUseCase: GetPaymentLinkUseCase,
    private val getTopUpPaymentLinkUseCase: GetTopUpPaymentLinkUseCase,
    private val events: OnboardingPaymentEvents,
    savedStateHandle: SavedStateHandle
) :
    BaseViewModel<OnboardingLocalPaymentState, OnboardingLocalPaymentSideEffect>(
        OnboardingLocalPaymentState
    ) {

    private var waitingResult: Boolean = false

    init {
        onViewCreatedRequestLink()
        handlePaymentRedirect()
        handleOkErrorButtonClick()
        handleOkBuyButtonClick()
        handleSupportClicks()
    }

    private fun handlePaymentResult() {
        when {
            args.paymentModel.resultCode.equals("AUTHORISED", true) -> {
                handleAuthorisedPayment()
            }
            args.paymentModel.refusalCode != null -> {
                handlePaymentRefusal()
            }
            args.paymentModel.error.hasError -> {
                when (args.paymentModel.error.errorInfo?.errorType) {
                    com.appcoins.wallet.billing.ErrorInfo.ErrorType.BILLING_ADDRESS -> {

                    }
                    else -> {
                        events.sendPaymentErrorEvent(args.transactionBuilder, args.paymentType)
                        sendSideEffect { OnboardingPaymentResultSideEffect.ShowPaymentError(args.paymentModel.error) }
                    }
                }
            }
            args.paymentModel.status == PaymentModel.Status.CANCELED -> {
                sendSideEffect { OnboardingPaymentResultSideEffect.NavigateBackToPaymentMethods }
            }
            else -> {
                sendSideEffect { OnboardingPaymentResultSideEffect.ShowPaymentError(args.paymentModel.error) }
            }
        }
    }

    private fun handleAuthorisedPayment() {
        adyenPaymentInteractor.getPaymentLinkUseCase(args.paymentModel.uid)
            .doOnNext { authorisedPaymentModel ->
                when {
                    authorisedPaymentModel.status == PaymentModel.Status.COMPLETED -> {
                        events.sendPaymentSuccessEvent(args.transactionBuilder, args.paymentType)
                        createBundle(
                            authorisedPaymentModel.hash,
                            authorisedPaymentModel.orderReference,
                            authorisedPaymentModel.purchaseUid
                        ).map { purchaseBundleModel ->
                            events.sendPaymentSuccessFinishEvents(args.transactionBuilder, args.paymentType)
                            sendSideEffect {
                                setOnboardingCompletedUseCase()
                                OnboardingPaymentResultSideEffect.ShowPaymentSuccess(
                                    purchaseBundleModel
                                )
                            }
                        }.subscribe()
                    }
                    isPaymentFailed(authorisedPaymentModel.status) -> {
                        events.sendPaymentErrorEvent(
                            args.transactionBuilder,
                            args.paymentType,
                            authorisedPaymentModel.error.errorInfo?.httpCode,
                            buildRefusalReason(
                                authorisedPaymentModel.status,
                                authorisedPaymentModel.error.errorInfo?.text
                            )
                        )
                        sendSideEffect {
                            OnboardingPaymentResultSideEffect.ShowPaymentError(
                                authorisedPaymentModel.error
                            )
                        }
                    }
                    else -> {
                        events.sendPaymentErrorEvent(
                            args.transactionBuilder,
                            args.paymentType,
                            authorisedPaymentModel.error.errorInfo?.httpCode,
                            buildRefusalReason(
                                authorisedPaymentModel.status,
                                authorisedPaymentModel.error.errorInfo?.text
                            )
                        )
                        sendSideEffect {
                            OnboardingPaymentResultSideEffect.ShowPaymentError(
                                authorisedPaymentModel.error
                            )
                        }
                    }
                }
            }.scopedSubscribe()
    }

    private fun handlePaymentRefusal() {
        var riskRules: String? = null
        args.paymentModel.refusalCode?.let { code ->
            when (code) {
                AdyenErrorCodeMapper.FRAUD -> {
                    handleFraudFlow(args.paymentModel.error, code)
                    riskRules = args.paymentModel.fraudResultIds.sorted().joinToString(separator = "-")
                }
                else -> sendSideEffect {
                    OnboardingPaymentResultSideEffect.ShowPaymentError(
                        args.paymentModel.error,
                        code
                    )
                }
            }
        }
        events.sendPaymentErrorEvent(
            args.transactionBuilder,
            args.paymentType,
            args.paymentModel.refusalCode,
            args.paymentModel.refusalReason,
            riskRules
        )
    }

}
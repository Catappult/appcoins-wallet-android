package com.asfoundation.wallet.onboarding_new_payment.local_payments


import android.net.Uri
import androidx.activity.result.ActivityResult

import androidx.lifecycle.SavedStateHandle
import com.appcoins.wallet.appcoins.rewards.ErrorInfo
import com.appcoins.wallet.appcoins.rewards.ErrorMapper
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetPaymentLinkUseCase
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetTopUpPaymentLinkUseCase
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.asfoundation.wallet.ui.iab.localpayments.LocalPaymentInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.Scheduler
import javax.inject.Inject


sealed class OnboardingLocalPaymentSideEffect : SideEffect {
    data class NavigateToWebView(val uri: String) : OnboardingLocalPaymentSideEffect()
    object NavigateBackToPaymentMethods : OnboardingLocalPaymentSideEffect()
    data class HandleWebViewResult(val uri: Uri) : OnboardingLocalPaymentSideEffect()
    object ShowLoading : OnboardingLocalPaymentSideEffect()
    object ShowError : OnboardingLocalPaymentSideEffect()
    object ShowSuccess : OnboardingLocalPaymentSideEffect()

    object ShowCompletablePayment : OnboardingLocalPaymentSideEffect()
    object ShowVerification : OnboardingLocalPaymentSideEffect()
}


object OnboardingLocalPaymentState : ViewState

@HiltViewModel
class OnboardingLocalPaymentViewModel @Inject constructor(
    private val getPaymentLinkUseCase: GetPaymentLinkUseCase,
    private val getTopUpPaymentLinkUseCase: GetTopUpPaymentLinkUseCase,
    private val events: OnboardingPaymentEvents,
    private val errorMapper: ErrorMapper,
    private val localPaymentInteractor: LocalPaymentInteractor,
    savedStateHandle: SavedStateHandle
) :
    BaseViewModel<OnboardingLocalPaymentState, OnboardingLocalPaymentSideEffect>(
        OnboardingLocalPaymentState
    ) {

    private var args: OnboardingLocalPaymentFragmentArgs =
        OnboardingLocalPaymentFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private lateinit var scheduler: Scheduler

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

    private fun handleSyncCompletedStatus(transactionResponse: Transaction): Completable {
        args.transactionBuilder.let { transaction ->
            return localPaymentInteractor.getCompletePurchaseBundle(
                transaction.type, transaction.fromAddress(), transaction.skuId,
                transactionResponse.metadata?.purchaseUid, transactionResponse.orderReference, transactionResponse.hash,
                scheduler
            )
                .doOnSuccess {
                    events.sendPaymentConclusionEvents(
                        transaction.fromAddress(), transaction.skuId, transaction.amount(),
                        transaction.type, transaction.chainId.toString()
                    )
                    events.sendRevenueEvent(args.transactionBuilder.amount().toString())
                }
                .flatMapCompletable {
                    Completable.fromAction { sendSideEffect { OnboardingLocalPaymentSideEffect.ShowCompletablePayment } }
                }
        }
    }

    private fun handleFraudFlow() {
        localPaymentInteractor.isWalletBlocked()
            .doOnSuccess { blocked ->
                if (blocked) {
                    localPaymentInteractor.isWalletVerified().doAfterSuccess {
                        if (it) {
                            sendSideEffect { OnboardingLocalPaymentSideEffect.ShowError }
                            //view.showError(R.string.purchase_error_wallet_block_code_403)
                        } else {
                            sendSideEffect { OnboardingLocalPaymentSideEffect.ShowVerification }
                        }
                    }.scopedSubscribe()
                } else {
                    sendSideEffect { OnboardingLocalPaymentSideEffect.ShowError }
                    //R.string.purchase_error_wallet_block_code_403
                }
            }.scopedSubscribe()
    }

    private fun getPaymentLink() {
        getPaymentLinkUseCase(
            data = args.transactionBuilder,
            currency = args.currency,
            packageName = args.transactionBuilder.productName
        ).doAfterSuccess {
            args.transactionBuilder.let {
                events.sendLocalNavigationToUrlEvents(
                    BuildConfig.APPLICATION_ID,
                    it.skuId,
                    it.amount().toString(),
                    it.type,
                    it.chainId.toString()
                )
            }
            sendSideEffect { OnboardingLocalPaymentSideEffect.NavigateToWebView(it) }
        }.scopedSubscribe()
    }

}
package com.asfoundation.wallet.onboarding.pending_payment

import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetModifiedCachedTransactionUseCase
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetOnboardingTransactionBuilderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import javax.inject.Inject

sealed class OnboardingPaymentSideEffect : SideEffect {
  data class ShowPaymentMethods(val transactionContent: TransactionContent) :
    OnboardingPaymentSideEffect()
}

data class OnboardingPaymentState(val transactionContent: Async<TransactionContent> = Async.Uninitialized) :
  ViewState

@HiltViewModel
class OnboardingPaymentViewModel @Inject constructor(
  private val getModifiedCachedTransactionUseCase: GetModifiedCachedTransactionUseCase,
  private val getOnboardingTransactionBuilderUseCase: GetOnboardingTransactionBuilderUseCase,
  private val billingAnalytics: BillingAnalytics
) :
  BaseViewModel<OnboardingPaymentState, OnboardingPaymentSideEffect>(OnboardingPaymentState()) {

  init {
    handleContent()
  }

  private fun handleContent() {
    Single.zip(
      getModifiedCachedTransactionUseCase(),
      getOnboardingTransactionBuilderUseCase()
    ) { cachedTransaction, transactionBuilder -> Pair(cachedTransaction, transactionBuilder) }
      .map { pair ->
        handlePurchaseStartAnalytics(pair.second)
        TransactionContent(
          pair.second,
          pair.first.packageName!!,
          pair.first.sku!!,
          pair.first.value,
          pair.first.currency!!
        )
      }
      .asAsyncToState {
        copy(transactionContent = it)
      }.doOnSuccess {
        sendSideEffect { OnboardingPaymentSideEffect.ShowPaymentMethods(it) }
      }
      .scopedSubscribe()
  }

  private fun handlePurchaseStartAnalytics(transactionBuilder: TransactionBuilder) {
    billingAnalytics.sendPurchaseStartWithoutDetailsEvent(
      transactionBuilder.domain,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      transactionBuilder.type,
      BillingAnalytics.RAKAM_PAYMENT_METHOD
    )
  }
}

data class TransactionContent(
  val transactionBuilder: TransactionBuilder,
  val packageName: String,
  val sku: String,
  val value: Double,
  val currency: String,
)
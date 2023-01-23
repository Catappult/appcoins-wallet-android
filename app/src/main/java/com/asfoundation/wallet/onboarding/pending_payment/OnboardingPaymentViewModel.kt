package com.asfoundation.wallet.onboarding.pending_payment

import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetEarningBonusUseCase
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
  private val bdsRepository: BdsRepository,
  private val getEarningBonusUseCase: GetEarningBonusUseCase,
  private val events: OnboardingPaymentEvents
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
      .flatMap { pair ->
        events.sendPurchaseStartWithoutDetailsEvent(pair.second)
        getEarningBonusUseCase(pair.second.domain, pair.second.amount())
          .map { forecastBonus ->
            TransactionContent(
              pair.second,
              pair.first.packageName!!,
              pair.first.sku!!,
              pair.first.value,
              pair.first.currency!!,
              forecastBonus
            )
          }
      }
      .asAsyncToState {
        copy(transactionContent = it)
      }.doOnSuccess {
        sendSideEffect { OnboardingPaymentSideEffect.ShowPaymentMethods(it) }
      }
      .scopedSubscribe()
  }
}

data class TransactionContent(
  val transactionBuilder: TransactionBuilder,
  val packageName: String,
  val sku: String,
  val value: Double,
  val currency: String,
  val forecastBonus: ForecastBonusAndLevel
)
package com.asfoundation.wallet.onboarding.pending_payment

import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.onboarding.CachedTransactionRepository
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetEarningBonusUseCase
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetOnboardingTransactionBuilderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject

sealed class OnboardingPaymentSideEffect : SideEffect {
  data class ShowPaymentMethods(val transactionContent: TransactionContent) :
    OnboardingPaymentSideEffect()
}

data class OnboardingPaymentState(val transactionContent: Async<TransactionContent> = Async.Uninitialized) :
  ViewState

@HiltViewModel
class OnboardingPaymentViewModel @Inject constructor(
  private val getOnboardingTransactionBuilderUseCase: GetOnboardingTransactionBuilderUseCase,
  private val bdsRepository: BdsRepository,
  private val cachedTransactionRepository: CachedTransactionRepository,
  private val getEarningBonusUseCase: GetEarningBonusUseCase,
  private val events: OnboardingPaymentEvents
) :
  BaseViewModel<OnboardingPaymentState, OnboardingPaymentSideEffect>(OnboardingPaymentState()) {

  init {
    handleContent()
  }

  private fun handleContent() {
    cachedTransactionRepository.getCachedTransaction()
      .flatMap { cachedTransaction ->
        getOnboardingTransactionBuilderUseCase(cachedTransaction)
          .flatMap { transactionBuilder ->
            bdsRepository.getSkuDetails(
              cachedTransaction.packageName!!,
              mutableListOf(cachedTransaction.sku!!),
              BillingSupportedType.INAPP
            ).flatMap { products ->
              val modifiedCachedTransaction = cachedTransaction.copy(
                value = products.first().transactionPrice.amount,
                currency = products.first().transactionPrice.currency
              )
              val modifiedTransactionBuilder =
                transactionBuilder.amount(BigDecimal(products.first().transactionPrice.appcoinsAmount))
              events.sendPurchaseStartWithoutDetailsEvent(modifiedTransactionBuilder)
              getEarningBonusUseCase(
                modifiedTransactionBuilder.domain,
                modifiedTransactionBuilder.amount()
              ).map { forecastBonus ->
                TransactionContent(
                  modifiedTransactionBuilder,
                  modifiedCachedTransaction.packageName!!,
                  modifiedCachedTransaction.sku!!,
                  modifiedCachedTransaction.value,
                  modifiedCachedTransaction.currency!!,
                  forecastBonus
                )
              }
            }
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
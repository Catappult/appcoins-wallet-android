package com.asfoundation.wallet.onboarding.pending_payment

import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.core.network.microservices.model.BillingSupportedType
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.onboarding.CachedTransactionRepository
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetEarningBonusUseCase
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetOnboardingTransactionBuilderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
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

  fun handleContent() {
    cachedTransactionRepository.getCachedTransaction()
      .flatMap { cachedTransaction ->
        Single.zip(
          getOnboardingTransactionBuilderUseCase(cachedTransaction),
          bdsRepository.getSkuDetails(
            cachedTransaction.packageName!!,
            mutableListOf(cachedTransaction.sku!!),
            BillingSupportedType.INAPP
          )
        ) { transactionBuilder, products ->
          Triple(cachedTransaction, transactionBuilder, products)
        }
      }
      .retryWhen { it.take(10).delay(200, TimeUnit.MILLISECONDS) }
      .flatMap { (cachedTransaction, transactionBuilder, products) ->
        val modifiedCachedTransaction = cachedTransaction.copy(
          value = products.first().transactionPrice.amount,
          currency = products.first().transactionPrice.currency
        )
        val modifiedTransactionBuilder =
          transactionBuilder.amount(BigDecimal(products.first().transactionPrice.appcoinsAmount))
        events.sendPurchaseStartWithoutDetailsEvent(modifiedTransactionBuilder)
        getEarningBonusUseCase(
          modifiedTransactionBuilder.domain,
          products.first().transactionPrice.amount.toBigDecimal(),
          products.first().transactionPrice.currency
        ).map { forecastBonus ->
          TransactionContent(
            modifiedTransactionBuilder,
            modifiedCachedTransaction.packageName!!,
            modifiedCachedTransaction.sku!!,
            products.first().title,
            modifiedCachedTransaction.value,
            modifiedCachedTransaction.currency!!,
            products.first().transactionPrice.currencySymbol,
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
  val skuTitle: String,
  val value: Double,
  val currency: String,
  val currencySymbol: String,
  val forecastBonus: ForecastBonusAndLevel
)
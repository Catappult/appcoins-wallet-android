package com.asfoundation.wallet.onboarding.pending_payment

import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.core.analytics.analytics.partners.AddressService
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.network.backend.model.enums.RefundDisclaimerEnum
import com.appcoins.wallet.core.network.microservices.model.BillingSupportedType
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetShowRefundDisclaimerCodeUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.SetCachedShowRefundDisclaimerUseCase
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.onboarding.CachedTransactionRepository
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetEarningBonusUseCase
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetOnboardingTransactionBuilderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import java.io.Serializable
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed class OnboardingPaymentSideEffect : SideEffect {
  data class ShowPaymentMethods(val transactionContent: TransactionContent) :
    OnboardingPaymentSideEffect()

  data class showOrHideRefundDisclaimer(val showOrHideRefundDisclaimer: Boolean) :
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
  private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
  private val events: OnboardingPaymentEvents,
  private val getShowRefundDisclaimerCodeUseCase: GetShowRefundDisclaimerCodeUseCase,
  private var setCachedShowRefundDisclaimerUseCase: SetCachedShowRefundDisclaimerUseCase,
  private var addressService: AddressService,
  val rxSchedulers: RxSchedulers
) :
  BaseViewModel<OnboardingPaymentState, OnboardingPaymentSideEffect>(OnboardingPaymentState()) {

  init {
    handleContent()
    updateRefundDisclaimerValue()
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
          ),
          addressService.getAttribution(cachedTransaction.packageName)
        ) { transactionBuilder, products, attribution ->
          Quadruple(cachedTransaction, transactionBuilder, products, attribution)
        }
      }
      .retryWhen { it.take(10).delay(200, TimeUnit.MILLISECONDS) }
      .flatMap { (cachedTransaction, transactionBuilder, products, attribution) ->
        val modifiedCachedTransaction = cachedTransaction.copy(
          value = products.first().transactionPrice.amount,
          currency = products.first().transactionPrice.currency
        )
        val modifiedTransactionBuilder =
          transactionBuilder.amount(BigDecimal(products.first().transactionPrice.appcoinsAmount))
        events.sendPurchaseStartEvent(modifiedTransactionBuilder, attribution.oemId)
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

  private fun updateRefundDisclaimerValue() {
    getShowRefundDisclaimerCodeUseCase().subscribeOn(rxSchedulers.io)
      .doOnSuccess {
        if (it.showRefundDisclaimer == RefundDisclaimerEnum.SHOW_REFUND_DISCLAIMER.state) {
          setCachedShowRefundDisclaimerUseCase(true)
          sendSideEffect { OnboardingPaymentSideEffect.showOrHideRefundDisclaimer(true) }
        } else {
          setCachedShowRefundDisclaimerUseCase(false)
          sendSideEffect { OnboardingPaymentSideEffect.showOrHideRefundDisclaimer(false) }
        }
      }.scopedSubscribe({}, {
        it.printStackTrace()
      })
  }

  fun setOnboardingCompleted() {
    setOnboardingCompletedUseCase()
  }
}

data class Quadruple<A,B,C,D>(var first: A, var second: B, var third: C, var fourth: D): Serializable {
  override fun toString(): String = "($first, $second, $third, $fourth)"
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
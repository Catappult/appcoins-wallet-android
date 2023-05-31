package com.asfoundation.wallet.topup

import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.core.network.microservices.model.FeeEntity
import com.appcoins.wallet.core.network.microservices.model.FeeType
import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.asfoundation.wallet.backup.NotificationNeeded
import com.asfoundation.wallet.feature_flags.topup.TopUpDefaultValueUseCase
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import com.appcoins.wallet.feature.changecurrency.data.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethod
import com.asfoundation.wallet.ui.iab.PaymentMethodFee
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

class TopUpInteractor @Inject constructor(
  private val repository: BdsRepository,
  private val conversionService: LocalCurrencyConversionService,
  private val gamificationInteractor: GamificationInteractor,
  private val topUpValuesService: TopUpValuesService,
  private var walletBlockedInteract: WalletBlockedInteract,
  private var inAppPurchaseInteractor: InAppPurchaseInteractor,
  private var supportInteractor: SupportInteractor,
  private var topUpDefaultValueUseCase: TopUpDefaultValueUseCase,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase
) {

  private val chipValueIndexMap: LinkedHashMap<FiatValue, Int> = LinkedHashMap()
  private var limitValues: TopUpLimitValues = TopUpLimitValues()

  fun getPaymentMethods(value: String, currency: String): Single<List<PaymentMethod>> =
    repository.getPaymentMethods(value, currency, "fiat", true, "TOPUP")
      .map { mapPaymentMethods(it) }

  fun isWalletBlocked() = walletBlockedInteract.isWalletBlocked()

  fun incrementAndValidateNotificationNeeded(): Single<NotificationNeeded> =
    inAppPurchaseInteractor.incrementAndValidateNotificationNeeded()

  fun showSupport(): Completable = gamificationInteractor.getUserLevel()
    .flatMapCompletable { level ->
      inAppPurchaseInteractor.walletAddress
        .flatMapCompletable { wallet ->
          supportInteractor.showSupport(wallet, level)
        }
    }

  fun convertAppc(value: String): Single<com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue> =
    conversionService.getAppcToLocalFiat(value, 2)

  fun convertLocal(currency: String, value: String, scale: Int): Single<com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue> =
    conversionService.getFiatToAppc(currency, value, scale)

  private fun mapPaymentMethods(
    paymentMethods: List<PaymentMethodEntity>
  ): List<PaymentMethod> = paymentMethods.map {
    PaymentMethod(
      it.id, it.label, it.iconUrl, it.async,
      mapPaymentMethodFee(it.fee), it.isAvailable(), null
    )
  }

  private fun mapPaymentMethodFee(feeEntity: FeeEntity?): PaymentMethodFee? = feeEntity?.let {
    if (feeEntity.type === FeeType.EXACT) {
      PaymentMethodFee(true, feeEntity.cost?.value, feeEntity.cost?.currency)
    } else {
      PaymentMethodFee(false, null, null)
    }
  }

  fun getEarningBonus(packageName: String, amount: BigDecimal): Single<ForecastBonusAndLevel> =
    getCurrentPromoCodeUseCase().flatMap {
      gamificationInteractor.getEarningBonus(packageName, amount, it.code)
    }

  fun getLimitTopUpValues(): Single<TopUpLimitValues> =
    if (limitValues.maxValue != TopUpLimitValues.INITIAL_LIMIT_VALUE &&
      limitValues.minValue != TopUpLimitValues.INITIAL_LIMIT_VALUE
    ) {
      Single.just(limitValues)
    } else {
      topUpValuesService.getLimitValues()
        .doOnSuccess { if (!it.error.hasError) cacheLimitValues(it) }
    }

  fun getDefaultValues(): Single<TopUpValuesModel> = if (chipValueIndexMap.isNotEmpty()) {
    Single.just(TopUpValuesModel(ArrayList(chipValueIndexMap.keys)))
  } else {
    topUpValuesService.getDefaultValues()
      .doOnSuccess { if (!it.error.hasError) cacheChipValues(it.values) }
  }

  fun cleanCachedValues() {
    limitValues = TopUpLimitValues()
    chipValueIndexMap.clear()
  }

  fun isBonusValidAndActive(): Boolean = gamificationInteractor.isBonusActiveAndValid()

  fun isBonusValidAndActive(forecastBonusAndLevel: ForecastBonusAndLevel): Boolean =
    gamificationInteractor.isBonusActiveAndValid(forecastBonusAndLevel)

  private fun cacheChipValues(chipValues: List<com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue>) {
    for (index in chipValues.indices) {
      chipValueIndexMap[chipValues[index]] = index
    }
  }

  private fun cacheLimitValues(values: TopUpLimitValues) {
    limitValues = TopUpLimitValues(values.minValue, values.maxValue)
  }

  fun getWalletAddress(): Single<String> = inAppPurchaseInteractor.walletAddress

  fun getABTestingExperiment(): Single<Int> = Single.create {
    MainScope().launch {
      it.onSuccess(topUpDefaultValueUseCase.getVariant() ?: 1)
    }
  }

  fun setABTestingExperimentImpression() = MainScope().launch {
    topUpDefaultValueUseCase.setImpressed()
  }

  fun setABTestingExperimentTopUpEvent(appcValue: Double) = MainScope().launch {
    topUpDefaultValueUseCase.setTopUpWith(appcValue)
  }
}
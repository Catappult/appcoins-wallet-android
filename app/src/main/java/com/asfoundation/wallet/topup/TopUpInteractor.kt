package com.asfoundation.wallet.topup

import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.bdsbilling.repository.entity.PaymentMethodEntity
import com.appcoins.wallet.gamification.repository.ForecastBonus
import com.asfoundation.wallet.service.LocalCurrencyConversionService
import com.asfoundation.wallet.topup.paymentMethods.PaymentMethodData
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal

class TopUpInteractor(private val repository: BdsRepository,
                      private val conversionService: LocalCurrencyConversionService,
                      private val gamificationInteractor: GamificationInteractor,
                      private val topUpValuesService: TopUpValuesService,
                      private val chipValueIndexMap: LinkedHashMap<FiatValue, Int>,
                      private var limitValues: TopUpLimitValues) {


  fun getPaymentMethods(): Single<List<PaymentMethodData>> {
    return repository.getPaymentMethods(type = "fiat")
        .map { methods ->
          mapPaymentMethods(methods)
        }
  }

  fun convertAppc(value: String): Observable<FiatValue> {
    return conversionService.getAppcToLocalFiat(value, 2)
  }

  fun convertLocal(currency: String, value: String, scale: Int): Observable<FiatValue> {
    return conversionService.getLocalToAppc(currency, value, scale)
  }

  private fun mapPaymentMethods(
      paymentMethods: List<PaymentMethodEntity>): List<PaymentMethodData> {
    val paymentMethodsData: MutableList<PaymentMethodData> = mutableListOf()
    paymentMethods.forEach {
      paymentMethodsData.add(PaymentMethodData(it.iconUrl, it.label, it.id))
    }
    return paymentMethodsData
  }

  fun getEarningBonus(packageName: String, amount: BigDecimal): Single<ForecastBonus> {
    return gamificationInteractor.getEarningBonus(packageName, amount)
  }

  fun getLimitTopUpValues(): Single<TopUpLimitValues> {
    return if (limitValues.maxValue.currency != "" && limitValues.minValue.currency != "") {
      Single.just(limitValues)
    } else {
      topUpValuesService.getLimitValues()
          .doOnSuccess { cacheLimitValues(it) }
    }
  }

  fun getDefaultValues(): Single<List<FiatValue>> {
    return if (chipValueIndexMap.isNotEmpty()) {
      Single.just(ArrayList(chipValueIndexMap.keys))
    } else {
      topUpValuesService.getDefaultValues()
          .doOnSuccess { cacheChipValues(it) }
    }
  }

  fun getChipIndex(value: FiatValue): Single<Int> {
    return if (chipValueIndexMap.isNotEmpty() && chipValueIndexMap.containsKey(value)) {
      Single.just(chipValueIndexMap[value])
    } else {
      if (chipValueIndexMap.isEmpty()) {
        topUpValuesService.getDefaultValues()
            .doOnSuccess { cacheChipValues(it) }
      }
      Single.just(-1)
    }
  }

  fun cleanCachedValues() {
    cleanCachedLimitValues()
    cleanCachedDefaultValues()
  }

  private fun cacheChipValues(chipValues: List<FiatValue>) {
    for (index in chipValues.indices) {
      chipValueIndexMap[chipValues[index]] = index
    }
  }

  private fun cacheLimitValues(values: TopUpLimitValues) {
    limitValues = TopUpLimitValues(values.minValue, values.maxValue)
  }

  private fun cleanCachedLimitValues() {
    limitValues = TopUpLimitValues()
  }

  private fun cleanCachedDefaultValues() {
    chipValueIndexMap.clear()
  }
}
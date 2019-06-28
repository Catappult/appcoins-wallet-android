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
                      private val gamificationInteractor: GamificationInteractor) {

  fun getPaymentMethods(): Single<List<PaymentMethodData>> {
    return repository.getPaymentMethods(type = "fiat")
        .map { methods ->
          mapPaymentMethods(methods)
        }
  }

  fun getLocalCurrency(): Single<LocalCurrency> {
    return conversionService.localCurrency
        .map { value ->
          LocalCurrency(value.symbol, value.currency)
        }
  }

  fun convertAppc(value: String): Observable<FiatValue> {
    return conversionService.getAppcToLocalFiat(value, 2)
  }

  fun convertLocal(currency: String, value: String): Observable<FiatValue> {
    return conversionService.getLocalToAppc(currency, value)
  }

  private fun mapPaymentMethods(
      paymentMethods: List<PaymentMethodEntity>): List<PaymentMethodData> {
    var paymentMethodsData: MutableList<PaymentMethodData> = mutableListOf()
    paymentMethods.forEach {
      paymentMethodsData.add(PaymentMethodData(it.iconUrl, it.label, it.id))
    }
    return paymentMethodsData
  }

  fun getEarningBonus(packageName: String, amount: BigDecimal): Single<ForecastBonus> {
    return gamificationInteractor.getEarningBonus(packageName, amount)
  }

}

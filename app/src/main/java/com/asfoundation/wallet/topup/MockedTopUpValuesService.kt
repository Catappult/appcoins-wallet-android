package com.asfoundation.wallet.topup

import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Single
import java.math.BigDecimal

class MockedTopUpValuesService {

  fun getMinTopUpValue(): Single<FiatValue> {
    return Single.just(FiatValue(BigDecimal(2), "EUR", "€"))
  }

  fun getMaxTopUpValue(): Single<FiatValue> {
    return Single.just(FiatValue(BigDecimal(120), "EUR", "€"))
  }

  fun getDefaultValues(): Single<List<FiatValue>> {
    val defaultValues = arrayListOf<FiatValue>()
    defaultValues.addAll(
        listOf(FiatValue(BigDecimal(5), "EUR", "€"), FiatValue(BigDecimal(10), "EUR", "€"),
            FiatValue(BigDecimal(20), "EUR", "€"), FiatValue(BigDecimal(50), "EUR", "€")))
    return Single.just(defaultValues)
  }

}
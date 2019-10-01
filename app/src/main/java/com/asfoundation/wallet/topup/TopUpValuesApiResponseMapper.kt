package com.asfoundation.wallet.topup

import com.asfoundation.wallet.ui.iab.FiatValue
import java.math.BigDecimal


class TopUpValuesApiResponseMapper {

  fun map(defaultValues: TopUpDefaultValuesResponse): List<FiatValue>? {
    return ArrayList(defaultValues.items.map {
      FiatValue(BigDecimal(it.price.fiat.value), it.price.fiat.currency.code,
          it.price.fiat.currency.sign)
    })
  }

  fun mapMin(limitValues: TopUpLimitValuesResponse): TopUpLimitValue? {
    return TopUpLimitValue(FiatValue(BigDecimal(limitValues.values.min), limitValues.currency.code,
        limitValues.currency.sign))
  }

  fun mapMax(limitValues: TopUpLimitValuesResponse): TopUpLimitValue? {
    return TopUpLimitValue(FiatValue(BigDecimal(limitValues.values.max), limitValues.currency.code,
        limitValues.currency.sign))
  }
}
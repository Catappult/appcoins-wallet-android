package com.asfoundation.wallet.topup

import com.asfoundation.wallet.ui.iab.FiatValue
import java.math.BigDecimal


class TopUpValuesApiResponseMapper {

  fun map(defaultValues: TopUpDefaultValuesResponse): TopUpValuesModel {
    return TopUpValuesModel(ArrayList(defaultValues.items.map {
      FiatValue(BigDecimal(it.price.fiat.value), it.price.fiat.currency.code,
          it.price.fiat.currency.sign)
    }))
  }

  fun mapValues(limitValuesResponse: TopUpLimitValuesResponse): TopUpLimitValues {
    return TopUpLimitValues(
        FiatValue(BigDecimal(limitValuesResponse.values.min), limitValuesResponse.currency.code,
            limitValuesResponse.currency.sign),
        FiatValue(BigDecimal(limitValuesResponse.values.max), limitValuesResponse.currency.code,
            limitValuesResponse.currency.sign))
  }
}
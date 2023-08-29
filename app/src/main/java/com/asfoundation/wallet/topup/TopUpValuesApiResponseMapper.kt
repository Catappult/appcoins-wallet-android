package com.asfoundation.wallet.topup

import com.appcoins.wallet.core.network.microservices.model.TopUpDefaultValuesResponse
import com.appcoins.wallet.core.network.microservices.model.TopUpLimitValuesResponse
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import java.math.BigDecimal
import javax.inject.Inject


class TopUpValuesApiResponseMapper @Inject constructor(){

  fun map(defaultValues: TopUpDefaultValuesResponse): TopUpValuesModel {
    return TopUpValuesModel(ArrayList(defaultValues.items.map {
      FiatValue(
        BigDecimal(it.price.fiat.value), it.price.fiat.currency.code,
        it.price.fiat.currency.sign
      )
    }))
  }

  fun mapValues(limitValuesResponse: TopUpLimitValuesResponse): TopUpLimitValues {
    return TopUpLimitValues(
      FiatValue(
        BigDecimal(limitValuesResponse.values.min), limitValuesResponse.currency.code,
        limitValuesResponse.currency.sign
      ),
      FiatValue(
        BigDecimal(limitValuesResponse.values.max), limitValuesResponse.currency.code,
        limitValuesResponse.currency.sign
      )
    )
  }
}
package com.asfoundation.wallet.topup

import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import java.math.BigDecimal

class TopUpValuesService(private val api: TopUpValuesApi,
                         private val responseMapper: TopUpValuesApiResponseMapper) {

  fun getDefaultValues(): Single<TopUpValuesModel> {
    return api.getDefaultValues(BuildConfig.APPLICATION_ID)
        .map { responseMapper.map(it) }
        .onErrorReturn { createErrorValuesList() }
  }

  fun getLimitValues(): Single<TopUpLimitValues> {
    return api.getInputLimitValues(BuildConfig.APPLICATION_ID)
        .map { responseMapper.mapValues(it) }
        .onErrorReturn { TopUpLimitValues() }
  }

  private fun createErrorValuesList(): TopUpValuesModel {
    return TopUpValuesModel(listOf(FiatValue(BigDecimal(-1), "", ""),
        FiatValue(BigDecimal(-2), "", ""),
        FiatValue(BigDecimal(-3), "", ""),
        FiatValue(BigDecimal(-4), "", "")), true)
  }

  interface TopUpValuesApi {
    @GET("product/8.20180518/topup/billing/domains/{packageName}")
    fun getInputLimitValues(@Path("packageName")
                            packageName: String): Single<TopUpLimitValuesResponse>

    @GET("product/8.20180518/topup/billing/domains/{packageName}/skus")
    fun getDefaultValues(@Path("packageName")
                         packageName: String): Single<TopUpDefaultValuesResponse>
  }
}
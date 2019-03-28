package com.asfoundation.wallet.billing.share

import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

class BdsShareLinkRepository(private var api: BdsShareLinkApi) : ShareLinkRepository {

  override fun getLink(domain: String, skuId: String?, message: String?,
                       walletAddress: String,
                       originalAmount: String?,
                       originalCurrency: String?): Single<String> {
    return api.getPaymentLink(
        ShareLinkData(domain, skuId, walletAddress, message, originalAmount, originalCurrency))
        .map { it.url }
  }

  interface BdsShareLinkApi {

    @POST("deeplink/8.20190326/topup/inapp/products")
    fun getPaymentLink(@Body data: ShareLinkData): Single<GetPaymentLinkResponse>
  }
}

data class ShareLinkData(@SerializedName("package") var packageName: String, var sku: String?,
                         @SerializedName("wallet_address")
                         var walletAddress: String,
                         var message: String?,
                         @SerializedName("price.value") var amount: String?,
                         @SerializedName("price.currency") var currency: String?)

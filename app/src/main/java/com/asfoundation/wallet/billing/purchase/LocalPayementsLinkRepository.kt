package com.asfoundation.wallet.billing.purchase

import com.asfoundation.wallet.billing.share.GetPaymentLinkResponse
import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

class LocalPayementsLinkRepository(private var api: DeepLinkApi) : InAppDeepLinkRepository {

  override fun getDeepLink(domain: String, skuId: String?,
                           userWalletAddress: String,
                           originalAmount: String?, originalCurrency: String?,
                           paymentMethod: String,
                           developerWalletAddress: String): Single<String> {
    return api.getDeepLink(
        DeepLinkData(domain, skuId, userWalletAddress, null, originalAmount, originalCurrency,
            paymentMethod, developerWalletAddress, null, null, null,
            null, null))
        .map { it.url }
  }

  interface DeepLinkApi {

    @POST("deeplink/8.20190101/inapp/product/purchases")
    fun getDeepLink(@Body data: DeepLinkData): Single<GetPaymentLinkResponse>
  }
}

data class DeepLinkData(@SerializedName("package") var packageName: String,
                        var sku: String?, @SerializedName("wallets.user")
                        var userWalletAddress: String,
                        var message: String?, @SerializedName("price.value")
                        var amount: String?, @SerializedName("price.currency")
                        var currency: String?, var method: String,
                        @SerializedName("wallets.developer") var developerWalletAddress: String,
                        @SerializedName("callback_url") var callback: String?,
                        var metadata: String?, var reference: String?,
                        @SerializedName("wallets.store") var storeWalletAddress: String?,
                        @SerializedName("wallets.oem") var oemWalletAddress: String?)

package com.asfoundation.wallet.billing.share

import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject

@BoundTo(supertype = ShareLinkRepository::class)
class BdsShareLinkRepository @Inject constructor(private var api: BdsShareLinkApi) :
    ShareLinkRepository {

  override fun getLink(domain: String, skuId: String?, message: String?, walletAddress: String,
                       originalAmount: String?, originalCurrency: String?,
                       paymentMethod: String): Single<String> {
    return api.getPaymentLink(
        ShareLinkData(domain, skuId, walletAddress, message, originalAmount, originalCurrency,
            paymentMethod))
        .map { it.url }
  }

  interface BdsShareLinkApi {

    @POST("deeplink/8.20190326/topup/inapp/products")
    fun getPaymentLink(@Body data: ShareLinkData): Single<GetPaymentLinkResponse>
  }
}

data class ShareLinkData(@SerializedName("package") var packageName: String,
                         var sku: String?, @SerializedName("wallet_address")
                         var walletAddress: String,
                         var message: String?, @SerializedName("price.value")
                         var amount: String?, @SerializedName("price.currency")
                         var currency: String?, var method: String)

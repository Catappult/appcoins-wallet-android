package com.appcoins.wallet.billing.carrierbilling

import com.appcoins.wallet.billing.carrierbilling.request.CarrierTransactionBody
import com.appcoins.wallet.billing.carrierbilling.response.CarrierTransactionResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

class CarrierBillingRepository(private val api: CarrierBillingApi,
                               private val mapper: CarrierResponseMapper,
                               packageName: String) {

  private val RETURN_URL = "http://${packageName}/return/carrier_billing"
  private val METHOD = "onebip"

  fun makePayment(walletAddress: String, walletSignature: String,
                  phoneNumber: String, packageName: String, origin: String?, sku: String?,
                  reference: String?, transactionType: String, currency: String,
                  value: String, developerWallet: String, oemWallet: String, storeWallet: String,
                  userWallet: String): Single<CarrierPaymentModel> {
    return api.makePayment(walletAddress, walletSignature,
        CarrierTransactionBody(phoneNumber, RETURN_URL, METHOD, packageName, origin, sku, reference,
            transactionType, currency, value, developerWallet, oemWallet, storeWallet, userWallet))
        .map { response -> mapper.mapPayment(response) }
        .onErrorReturn { e -> mapper.mapPaymentError(e) }
  }

  interface CarrierBillingApi {
    @POST("transactions")
    fun makePayment(@Query("wallet.address") walletAddress: String,
                    @Query("wallet.signature") walletSignature: String,
                    @Body carrierTransactionBody: CarrierTransactionBody)
        : Single<CarrierTransactionResponse>
  }

}
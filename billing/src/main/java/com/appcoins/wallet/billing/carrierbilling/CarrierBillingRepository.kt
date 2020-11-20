package com.appcoins.wallet.billing.carrierbilling

import com.appcoins.wallet.billing.carrierbilling.request.CarrierTransactionBody
import com.appcoins.wallet.billing.carrierbilling.response.CarrierCreateTransactionResponse
import com.appcoins.wallet.billing.common.response.TransactionResponse
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

class CarrierBillingRepository(private val api: CarrierBillingApi,
                               private val mapper: CarrierResponseMapper,
                               packageName: String) {

  companion object {
    private const val METHOD = "onebip"
  }

  private val RETURN_URL = "http://${packageName}/return/carrier_billing"

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

  fun getPayment(uid: String, walletAddress: String,
                 walletSignature: String): Observable<CarrierPaymentModel> {
    return api.getPayment(uid, walletAddress, walletSignature)
        .map { response -> mapper.mapPayment(response) }
        .onErrorReturn { e -> mapper.mapPaymentError(e) }
  }

  fun cancelPayment(uid: String, walletAddress: String, walletSignature: String): Single<Boolean> {
    return api.cancelPayment(uid, walletAddress, walletSignature)
        .map { response -> response.code() == 200 }
        .onErrorReturn { false }
  }

  interface CarrierBillingApi {
    @POST("transactions")
    fun makePayment(@Query("wallet.address") walletAddress: String,
                    @Query("wallet.signature") walletSignature: String,
                    @Body carrierTransactionBody: CarrierTransactionBody)
        : Single<CarrierCreateTransactionResponse>

    @GET("transactions/{uid}")
    fun getPayment(@Path("uid") uid: String,
                   @Query("wallet.address") walletAddress: String,
                   @Query("wallet.signature")
                   walletSignature: String): Observable<TransactionResponse>

    @POST("transactions/{uid}/cancel")
    fun cancelPayment(@Path("uid") uid: String,
                      @Query("wallet.address") walletAddress: String,
                      @Query("wallet.signature")
                      walletSignature: String): Single<Response<ResponseBody>>

  }

}
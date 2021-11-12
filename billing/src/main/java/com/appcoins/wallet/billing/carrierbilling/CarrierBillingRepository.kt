package com.appcoins.wallet.billing.carrierbilling

import com.appcoins.wallet.billing.carrierbilling.request.CarrierTransactionBody
import com.appcoins.wallet.billing.carrierbilling.response.CarrierCreateTransactionResponse
import com.appcoins.wallet.billing.carrierbilling.response.CountryListResponse
import com.appcoins.wallet.billing.common.response.TransactionResponse
import com.appcoins.wallet.commons.Logger
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.*

class CarrierBillingRepository(private val api: CarrierBillingApi,
                               private val preferences: CarrierBillingPreferencesRepository,
                               private val mapper: CarrierResponseMapper,
                               packageName: String,
                               private val logger: Logger) {

  companion object {
    private const val METHOD = "onebip"
  }

  private val RETURN_URL = "https://${packageName}/return/carrier_billing"

  fun makePayment(walletAddress: String, walletSignature: String,
                  phoneNumber: String, packageName: String, origin: String?, sku: String?,
                  reference: String?, transactionType: String, currency: String,
                  value: String, developerWallet: String?, entityOemId: String?,
                  entityDomain: String?,
                  userWallet: String?, referrerUrl: String?, developerPayload: String?,
                  callbackUrl: String?): Single<CarrierPaymentModel> {
    return api.makePayment(walletAddress, walletSignature,
        CarrierTransactionBody(phoneNumber, RETURN_URL, METHOD, packageName, origin, sku, reference,
            transactionType, currency, value, developerWallet, entityOemId, entityDomain,
            userWallet,
            referrerUrl, developerPayload, callbackUrl))
        .map { response -> mapper.mapPayment(response) }
        .onErrorReturn { e ->
          logger.log("CarrierBillingRepository", e)
          mapper.mapPaymentError(e)
        }
  }

  fun getPayment(uid: String, walletAddress: String,
                 walletSignature: String): Observable<CarrierPaymentModel> {
    return api.getPayment(uid, walletAddress, walletSignature)
        .map { response -> mapper.mapPayment(response) }
        .onErrorReturn { e ->
          logger.log("CarrierBillingRepository", e)
          mapper.mapPaymentError(e)
        }
  }

  fun retrieveAvailableCountryList(): Single<AvailableCountryListModel> {
    return api.getAvailableCountryList()
        .map { mapper.mapList(it) }
        .onErrorReturn { AvailableCountryListModel() }
  }

  fun savePhoneNumber(phoneNumber: String) = preferences.savePhoneNumber(phoneNumber)

  fun forgetPhoneNumber() = preferences.forgetPhoneNumber()

  fun retrievePhoneNumber() = preferences.retrievePhoneNumber()

  interface CarrierBillingApi {
    @POST("gateways/dimoco/transactions")
    fun makePayment(@Query("wallet.address") walletAddress: String,
                    @Query("wallet.signature") walletSignature: String,
                    @Body carrierTransactionBody: CarrierTransactionBody)
        : Single<CarrierCreateTransactionResponse>

    @GET("gateways/dimoco/transactions/{uid}")
    fun getPayment(@Path("uid") uid: String,
                   @Query("wallet.address") walletAddress: String,
                   @Query("wallet.signature")
                   walletSignature: String): Observable<TransactionResponse>

    @GET("dimoco/countries")
    fun getAvailableCountryList(): Single<CountryListResponse>
  }
}
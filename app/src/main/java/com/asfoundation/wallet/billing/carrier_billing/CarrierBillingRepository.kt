package com.asfoundation.wallet.billing.carrier_billing

import com.appcoins.wallet.billing.carrierbilling.AvailableCountryListModel
import com.appcoins.wallet.billing.carrierbilling.CarrierBillingPreferencesRepository
import com.appcoins.wallet.billing.carrierbilling.CarrierPaymentModel
import com.appcoins.wallet.core.network.microservices.model.CarrierTransactionBody
import com.appcoins.wallet.core.network.microservices.api.broker.CarrierBillingApi
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asf.wallet.BuildConfig
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class CarrierBillingRepository @Inject constructor(private val api: CarrierBillingApi,
                                                   private val preferences: CarrierBillingPreferencesRepository,
                                                   private val mapper: CarrierResponseMapper,
                                                   private val logger: Logger
) {

  companion object {
    private const val METHOD = "onebip"
  }

  private val RETURN_URL = "https://${BuildConfig.APPLICATION_ID}/return/carrier_billing"

  fun makePayment(walletAddress: String, walletSignature: String,
                  phoneNumber: String, packageName: String, origin: String?, sku: String?,
                  reference: String?, transactionType: String, currency: String,
                  value: String, developerWallet: String?, entityOemId: String?,
                  entityDomain: String?, entityPromoCode: String?,
                  userWallet: String?, referrerUrl: String?, developerPayload: String?,
                  callbackUrl: String?): Single<CarrierPaymentModel> {
    return api.makePayment(walletAddress, walletSignature,
        CarrierTransactionBody(phoneNumber, RETURN_URL, METHOD, packageName, origin, sku, reference,
            transactionType, currency, value, developerWallet, entityOemId, entityDomain,
            entityPromoCode,
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
}
package com.asfoundation.wallet.billing.amazonPay.repository

import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.microservices.api.broker.AmazonPayApi
import com.appcoins.wallet.core.network.microservices.model.AmazonPayChargePermissionResponse
import com.appcoins.wallet.core.network.microservices.model.AmazonPayCheckoutSessionRequest
import com.appcoins.wallet.core.network.microservices.model.AmazonPayPaymentRequest
import com.appcoins.wallet.core.network.microservices.model.AmazonPayTransaction
import com.appcoins.wallet.core.network.microservices.model.AmazonPrice
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.sharedpreferences.AmazonPayDataSource
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.HttpException
import javax.inject.Inject

class AmazonPayRepository @Inject constructor(
  private val amazonPayApi: AmazonPayApi,
  private val ewtObtainer: EwtAuthenticatorService,
  private val amazonPayDataSource: AmazonPayDataSource,
  private val rxSchedulers: RxSchedulers,
) {

  fun createTransaction(
    price: AmazonPrice, reference: String?, walletAddress: String,
    origin: String?, packageName: String?, metadata: String?, sku: String?,
    callbackUrl: String?, transactionType: String,
    entityOemId: String?, entityDomain: String?, entityPromoCode: String?,
    userWallet: String?, referrerUrl: String?,
    method: String?, chargePermissionId: String?
  ): Single<AmazonPayTransaction> {
    return ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
      .flatMap { ewt ->
        amazonPayApi.createTransaction(
          walletAddress = walletAddress,
          authorization = ewt,
          amazonPayRequest = AmazonPayPaymentRequest(
            price = price,
            reference = reference,
            origin = origin,
            metadata = metadata,
            sku = sku,
            callbackUrl = callbackUrl,
            entityOemId = entityOemId,
            entityDomain = entityDomain,
            entityPromoCode = entityPromoCode,
            referrerUrl = referrerUrl,
            method = method,
            domain = packageName,
            type = transactionType,
            returnUrl = HostProperties.AMAZON_PAY_REDIRECT_BASE_URL,
            channel = "ANDROID",
            chargePermissionId = chargePermissionId,
            guestWalletId = userWallet
          )
        )
          .map { response: AmazonPayTransaction ->
            AmazonPayTransaction(
              uid = response.uid,
              reference = response.reference,
              status = response.status,
              payload = response.payload,
              merchantId = response.merchantId,
              checkoutSessionId = response.checkoutSessionId,
              redirectUrl = response.redirectUrl,
              errorCode = null
            )
          }
          .onErrorReturn {
            val httpException = (it as? HttpException)
            val errorCode = httpException?.code()
            val errorContent = httpException?.response()?.errorBody()?.string()
            AmazonPayTransaction(
              null,
              null,
              null,
              null,
              null,
              errorCode.toString(),
              errorContent ?: ""
            )
          }
      }
  }

  fun patchAmazonPayCheckoutSession(
    uid: String?,
    walletAddress: String,
    amazonPayCheckoutSessionRequest: AmazonPayCheckoutSessionRequest
  ): Completable {
    return ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
      .flatMapCompletable { ewt ->
        amazonPayApi.updateCheckoutSessionId(
          uid,
          walletAddress,
          ewt,
          amazonPayCheckoutSessionRequest
        )
      }
  }

  fun getAmazonPayChargePermission(
    walletAddress: String,
  ): Single<AmazonPayChargePermissionResponse> {
    return ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
      .flatMap { ewt ->
        amazonPayApi.getChargePermission(
          walletAddress,
          ewt,
        )
      }
  }

  fun deleteAmazonPayChargePermission(
    walletAddress: String,
  ): Completable {
    return ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
      .flatMapCompletable { ewt ->
        amazonPayApi.deleteChargePermission(
          walletAddress,
          ewt,
        )
      }
  }


  fun saveResult(checkoutSessionId: String) {
    amazonPayDataSource.saveResult(checkoutSessionId)
  }

  fun consumeResult(): String {
    return amazonPayDataSource.consumeResult()
  }


  fun saveChargePermissionId(chargePermissionId: String?) {
    amazonPayDataSource.saveChargePermissionId(chargePermissionId)
  }

  fun getChargePermissionId(): String {
    return amazonPayDataSource.getChargePermissionId()
  }

  fun saveAmazonPayPaymentType(paymentType: String?) {
    amazonPayDataSource.saveAmazonPayPaymentType(paymentType)
  }

  fun getAmazonPayPaymentType(): String {
    return amazonPayDataSource.getAmazonPayPaymentType()
  }

}
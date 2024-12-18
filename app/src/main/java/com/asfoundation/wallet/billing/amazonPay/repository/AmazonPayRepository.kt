package com.asfoundation.wallet.billing.amazonPay.repository

import com.appcoins.wallet.core.network.microservices.api.broker.AmazonPayApi
import com.appcoins.wallet.core.network.microservices.model.AmazonPayChargePermissionResponse
import com.appcoins.wallet.core.network.microservices.model.AmazonPayCheckoutSessionRequest
import com.appcoins.wallet.core.network.microservices.model.AmazonPayPaymentRequest
import com.appcoins.wallet.core.network.microservices.model.AmazonPayTransaction
import com.appcoins.wallet.core.network.microservices.model.AmazonPrice
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.sharedpreferences.AmazonPayDataSource
import com.asf.wallet.BuildConfig
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.HttpException
import javax.inject.Inject

class AmazonPayRepository @Inject constructor(
  private val amazonPayApi: AmazonPayApi,
  private val amazonPayDataSource: AmazonPayDataSource,
  private val rxSchedulers: RxSchedulers,
) {

  val TEST_MFA = "MFA"

  fun createTransaction(
    price: AmazonPrice,
    reference: String?,
    walletAddress: String,
    origin: String?,
    packageName: String?,
    metadata: String?,
    sku: String?,
    callbackUrl: String?,
    transactionType: String,
    entityOemId: String?,
    entityDomain: String?,
    entityPromoCode: String?,
    referrerUrl: String?,
    method: String?,
    chargePermissionId: String?,
    guestWalletId: String?,
  ): Single<AmazonPayTransaction> {
    return amazonPayApi.createTransaction(
      walletAddress = walletAddress,
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
        guestWalletId = guestWalletId,
        testCase = if (BuildConfig.DEBUG)
          null //TEST_MFA // For testing in sandbox with MFA active
        else
          null
      )
    )
      .subscribeOn(rxSchedulers.io)
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
          uid = null,
          reference = null,
          status = null,
          payload = null,
          merchantId = null,
          checkoutSessionId = null,
          redirectUrl = null,
          errorCode = errorCode.toString(),
          errorContent = errorContent ?: ""
        )
      }
  }

  fun patchAmazonPayCheckoutSession(
    uid: String?,
    walletAddress: String,
    amazonPayCheckoutSessionRequest: AmazonPayCheckoutSessionRequest
  ): Completable =
    amazonPayApi.updateCheckoutSessionId(
      uid = uid,
      walletAddress = walletAddress,
      amazonPayRequest = amazonPayCheckoutSessionRequest
    ).subscribeOn(rxSchedulers.io)

  fun getAmazonPayChargePermission(
    walletAddress: String,
  ): Single<AmazonPayChargePermissionResponse> =
    amazonPayApi.getChargePermission(walletAddress)
      .subscribeOn(rxSchedulers.io)

  fun deleteAmazonPayChargePermission(
    walletAddress: String,
  ): Completable =
    amazonPayApi.deleteChargePermission(walletAddress)
      .subscribeOn(rxSchedulers.io)

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

}
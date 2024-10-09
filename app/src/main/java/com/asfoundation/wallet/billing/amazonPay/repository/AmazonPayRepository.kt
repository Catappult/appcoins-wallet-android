package com.asfoundation.wallet.billing.amazonPay.repository

import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.microservices.api.broker.AmazonPayApi
import com.appcoins.wallet.core.network.microservices.model.AmazonPayPaymentRequest
import com.appcoins.wallet.core.network.microservices.model.AmazonPayTransaction
import com.appcoins.wallet.core.network.microservices.model.AmazonPrice
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.sharedpreferences.AmazonPayDataSource
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
    price: AmazonPrice, walletAddress: String, packageName: String?, sku: String?,
    callbackUrl: String?, transactionType: String, method: String?, referrerUrl: String?
  ): Single<AmazonPayTransaction> {
    return ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
      .flatMap { ewt ->
        amazonPayApi.createTransaction(
          walletAddress = walletAddress,
          authorization = ewt,
          amazonPayRequest = AmazonPayPaymentRequest(
            callbackUrl = callbackUrl,
            domain = packageName,
            sku = sku,
            type = transactionType,
            price = price,
            referrerUrl = referrerUrl,
            method = method,
            returnUrl = "https://wallet.dev.appcoins.io/app/amazonpay/result",
            channel = "ANDROID",
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

  fun saveChromeResult(result: String, checkoutSessionId: String) {
    amazonPayDataSource.saveResult(result, checkoutSessionId)
  }

  fun consumeChromeResult(): Array<String> {
    return amazonPayDataSource.consumeResult()
  }

}
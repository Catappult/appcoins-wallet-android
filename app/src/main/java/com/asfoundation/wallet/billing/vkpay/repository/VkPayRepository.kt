package com.asfoundation.wallet.billing.vkpay.repository

import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.microservices.api.broker.VkPayApi
import com.appcoins.wallet.core.network.microservices.model.VkPayPaymentRequest
import com.appcoins.wallet.core.network.microservices.model.VkPayTransaction
import com.appcoins.wallet.core.network.microservices.model.VkPrice
import com.appcoins.wallet.core.network.microservices.model.VkTransactionResponse
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import io.reactivex.Single
import retrofit2.HttpException
import javax.inject.Inject

class VkPayRepository @Inject constructor(
  private val vkPayApi: VkPayApi,
  private val ewtObtainer: EwtAuthenticatorService,
  private val rxSchedulers: RxSchedulers,

  ) {

  fun createTransaction(
    price: VkPrice, reference: String?, walletAddress: String,
    origin: String?, packageName: String?, metadata: String?, sku: String?,
    callbackUrl: String?, transactionType: String,
    entityOemId: String?, entityDomain: String?, entityPromoCode: String?,
    userWallet: String?, referrerUrl: String?, method: String?, email: String, phone: String
  ): Single<VkPayTransaction> {
    return ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
      .flatMap { ewt ->
        vkPayApi.createTransaction(
          walletAddress = walletAddress,
          authorization = ewt,
          vkPayPaymentRequest = VkPayPaymentRequest(
            callbackUrl = callbackUrl,
            domain = packageName,
            metadata = metadata,
            origin = origin,
            sku = sku,
            reference = reference,
            type = transactionType,
            price = price,
            entityOemId = entityOemId,
            entityDomain = entityDomain,
            entityPromoCode = entityPromoCode,
            user = userWallet,
            referrerUrl = referrerUrl,
            method = method,
            email = email.ifBlank { null },
            phone = phone.ifBlank { null }
          )
        )
          .map { response: VkTransactionResponse ->
            VkPayTransaction(
              uid = response.uid,
              hash = response.hash,
              status = response.status,
              amount = response.amount,
              merchantId = response.merchantId ?: "0",
            )
          }
          .onErrorReturn {
            val httpException = (it as? HttpException)
            val errorCode = httpException?.code()
            val errorContent = httpException?.response()?.errorBody()?.string()
            VkPayTransaction(
              null,
              null,
              null,
              null,
              errorCode.toString(),
              errorContent ?: "",
              null,
            )
          }
      }
  }

}
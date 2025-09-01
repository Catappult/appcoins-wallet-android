package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.BillingRepository
import com.appcoins.wallet.bdsbilling.repository.entity.Product
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.core.network.microservices.model.BillingSupportedType
import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.network.microservices.model.Transaction
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigDecimal

class BdsRepository(private val remoteRepository: RemoteRepository) : BillingRepository {

  override fun registerAuthorizationProof(
    id: String,
    paymentType: String,
    walletAddress: String,
    walletSignature: String,
    productName: String?,
    packageName: String,
    priceValue: BigDecimal,
    entityOemId: String?,
    entityDomainId: String?,
    origin: String,
    type: String,
    developerPayload: String?,
    callback: String?,
    orderReference: String?,
    referrerUrl: String?,
    guestWalletId: String?
  ): Single<Transaction> = remoteRepository.registerAuthorizationProof(
    origin = origin,
    type = type,
    entityOemId = entityOemId,
    entityDomainId = entityDomainId,
    id = id,
    gateway = paymentType,
    walletAddress = walletAddress,
    productName = productName,
    packageName = packageName,
    priceValue = priceValue,
    developerPayload = developerPayload,
    callback = callback,
    orderReference = orderReference,
    referrerUrl = referrerUrl,
    guestWalletId = guestWalletId
  )

  override fun registerPaymentProof(
    paymentId: String,
    paymentType: String,
    walletAddress: String,
    signedData: String,
    paymentProof: String
  ): Completable = remoteRepository.registerPaymentProof(
    paymentId = paymentId,
    paymentType = paymentType,
    walletAddress = walletAddress,
    paymentProof = paymentProof
  )

  override fun isSupported(
    packageName: String,
    type: BillingSupportedType
  ): Single<Boolean> =
    remoteRepository.isBillingSupported(packageName)

  override fun getSkuDetails(
    packageName: String,
    skus: List<String>,
    type: BillingSupportedType
  ): Single<List<Product>> =
    if (BillingSupportedType.mapToProductType(type) == BillingSupportedType.INAPP) {
      remoteRepository.getSkuDetails(packageName, skus)
    } else {
      remoteRepository.getSkuDetailsSubs(packageName, skus)
    }

  override fun getSkuPurchase(
    packageName: String,
    skuId: String?,
    purchaseUid: String?,
    walletAddress: String,
    walletSignature: String,
    type: BillingSupportedType
  ): Single<Purchase> =
    if (BillingSupportedType.mapToProductType(type) == BillingSupportedType.INAPP) {
      remoteRepository.getSkuPurchase(
        packageName = packageName,
        skuId = skuId
      )
    } else {
      remoteRepository.getSkuPurchaseSubs(
        packageName = packageName,
        purchaseUid = purchaseUid!!,
        walletAddress = walletAddress,
        walletSignature = walletSignature
      )
    }

  override fun getSkuTransaction(
    packageName: String,
    skuId: String?,
    walletAddress: String,
    walletSignature: String,
    type: BillingSupportedType
  ): Single<Transaction> = remoteRepository.getSkuTransaction(
    packageName = packageName,
    skuId = skuId,
    walletAddress = walletAddress,
    walletSignature = walletSignature,
    type = type
  )
    .flatMap {
      if (it.items.isNotEmpty()) {
        return@flatMap Single.just(it.items[0])
      }
      return@flatMap Single.just(Transaction.notFound())
    }

  override fun getPurchases(
    packageName: String,
    walletAddress: String,
    walletSignature: String,
    type: BillingSupportedType
  ): Single<List<Purchase>> =
    if (BillingSupportedType.mapToProductType(type) == BillingSupportedType.INAPP) {
      remoteRepository.getPurchases(packageName)
    } else {
      remoteRepository.getPurchasesSubs(
        packageName = packageName,
        walletAddress = walletAddress,
        walletSignature = walletSignature
      )
    }

  override fun consumePurchases(
    packageName: String,
    purchaseToken: String,
    type: BillingSupportedType?
  ): Single<Boolean> {
    return remoteRepository.consumePurchase(
      packageName = packageName,
      purchaseToken = purchaseToken
    )
  }

  override fun getSubscriptionToken(
    packageName: String,
    skuId: String,
    walletAddress: String,
    externalBuyerReference: String?,
    isFreeTrial: Boolean?
  ): Single<String> =
    remoteRepository.getSubscriptionToken(
      domain = packageName,
      skuId = skuId,
      walletAddress = walletAddress,
      externalBuyerReference = externalBuyerReference,
      isFreeTrial = isFreeTrial
    )

  override fun getPaymentMethods(
    value: String?,
    currency: String?,
    currencyType: String?,
    direct: Boolean?,
    transactionType: String?,
    packageName: String?,
    entityOemId: String?,
    address: String?,
  ): Single<List<PaymentMethodEntity>> = remoteRepository.getPaymentMethods(
    value = value,
    currency = currency,
    currencyType = currencyType,
    direct = direct,
    transactionType = transactionType,
    packageName = packageName,
    entityOemId = entityOemId,
    address = address
  )
    .onErrorReturn {
      it.printStackTrace()
      ArrayList()
    }

  override fun getAppcoinsTransaction(
    uid: String,
    address: String,
    signedContent: String
  ): Single<Transaction> = remoteRepository.getAppcoinsTransaction(
    uid = uid,
    address = address,
    signedContent = signedContent
  )
}
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
  override fun getWallet(packageName: String): Single<String> =
    remoteRepository.getWallet(packageName).map { it.data.address }

  override fun registerAuthorizationProof(
    id: String,
    paymentType: String,
    walletAddress: String,
    walletSignature: String,
    productName: String?,
    packageName: String,
    priceValue: BigDecimal,
    developerWallet: String,
    entityOemId: String?,
    entityDomainId: String?,
    origin: String,
    type: String,
    developerPayload: String?,
    callback: String?,
    orderReference: String?,
    referrerUrl: String?
  ): Single<Transaction> = remoteRepository.registerAuthorizationProof(
    origin,
    type,
    entityOemId,
    entityDomainId,
    id,
    paymentType,
    walletAddress,
    productName,
    packageName,
    priceValue,
    developerWallet,
    developerPayload,
    callback,
    orderReference,
    referrerUrl,
    null
  )

  override fun registerPaymentProof(
    paymentId: String,
    paymentType: String,
    walletAddress: String,
    signedData: String,
    paymentProof: String
  ): Completable = remoteRepository.registerPaymentProof(
    paymentId,
    paymentType,
    walletAddress,
    paymentProof
  )

  override fun isSupported(packageName: String, type: BillingSupportedType): Single<Boolean> =
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
      remoteRepository.getSkuPurchase(packageName, skuId)
    } else {
      remoteRepository.getSkuPurchaseSubs(
        packageName,
        purchaseUid!!,
        walletAddress,
        walletSignature
      )
    }

  override fun getSkuTransaction(
    packageName: String,
    skuId: String?,
    walletAddress: String,
    walletSignature: String,
    type: BillingSupportedType
  ): Single<Transaction> = remoteRepository.getSkuTransaction(
    packageName,
    skuId,
    walletAddress,
    walletSignature,
    type
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
      remoteRepository.getPurchasesSubs(packageName, walletAddress, walletSignature)
    }

  override fun consumePurchases(
    packageName: String,
    purchaseToken: String,
    type: BillingSupportedType?
  ): Single<Boolean> {
    return remoteRepository.consumePurchase(
      packageName,
      purchaseToken
    )
  }

  override fun getSubscriptionToken(
    packageName: String,
    skuId: String,
    walletAddress: String,
    walletSignature: String
  ): Single<String> =
    remoteRepository.getSubscriptionToken(packageName, skuId, walletAddress, walletSignature)

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
    value,
    currency,
    currencyType,
    direct,
    transactionType,
    packageName,
    entityOemId,
    address
  )
    .onErrorReturn {
      it.printStackTrace()
      ArrayList()
    }

  override fun getAppcoinsTransaction(
    uid: String,
    address: String,
    signedContent: String
  ): Single<Transaction> = remoteRepository.getAppcoinsTransaction(uid, address, signedContent)
}
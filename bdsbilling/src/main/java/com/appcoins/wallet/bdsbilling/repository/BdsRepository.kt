package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.BillingRepository
import com.appcoins.wallet.bdsbilling.repository.entity.PaymentMethodEntity
import com.appcoins.wallet.bdsbilling.repository.entity.Product
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigDecimal

class BdsRepository(private val remoteRepository: RemoteRepository) : BillingRepository {
  override fun getWallet(packageName: String): Single<String> {
    return remoteRepository.getWallet(packageName)
        .map { it.data.address }
  }

  override fun registerAuthorizationProof(id: String, paymentType: String, walletAddress: String,
                                          walletSignature: String, productName: String?,
                                          packageName: String, priceValue: BigDecimal,
                                          developerWallet: String, storeWallet: String,
                                          origin: String, type: String, oemWallet: String,
                                          developerPayload: String?, callback: String?,
                                          orderReference: String?,
                                          referrerUrl: String?): Single<String> {
    return remoteRepository.registerAuthorizationProof(origin, type, oemWallet, id, paymentType,
        walletAddress, walletSignature, productName, packageName, priceValue, developerWallet,
        storeWallet, developerPayload, callback, orderReference, referrerUrl)
        .map { it.uid }
  }

  override fun registerPaymentProof(paymentId: String, paymentType: String, walletAddress: String,
                                    signedData: String, paymentProof: String): Completable {
    return remoteRepository.registerPaymentProof(paymentId, paymentType, walletAddress, signedData,
        paymentProof)
  }

  override fun isSupported(packageName: String, type: BillingSupportedType): Single<Boolean> {
    return if (type == BillingSupportedType.INAPP) {
      remoteRepository.isBillingSupported(packageName)
    } else {
      remoteRepository.isBillingSupportedSubs(packageName)
    }
  }

  override fun getSkuDetails(packageName: String, skus: List<String>,
                             type: BillingSupportedType): Single<List<Product>> {
    return if (type == BillingSupportedType.INAPP) {
      remoteRepository.getSkuDetails(packageName, skus)
    } else {
      remoteRepository.getSkuDetailsSubs(packageName, skus)
    }
  }

  override fun getSkuPurchase(packageName: String, skuId: String?, uid: String,
                              walletAddress: String, walletSignature: String,
                              type: BillingSupportedType): Single<Purchase> {
    return if (type == BillingSupportedType.INAPP) {
      remoteRepository.getSkuPurchase(packageName, skuId, walletAddress, walletSignature)
    } else {
      remoteRepository.getSkuPurchaseSubs(packageName, uid)
    }
  }

  override fun getSkuTransaction(packageName: String, skuId: String?, walletAddress: String,
                                 walletSignature: String,
                                 type: BillingSupportedType): Single<Transaction> {
    return remoteRepository.getSkuTransaction(packageName, skuId, walletAddress, walletSignature,
        type)
        .flatMap {
          if (it.items.isNotEmpty()) {
            return@flatMap Single.just(it.items[0])
          }
          return@flatMap Single.just(Transaction.notFound())
        }
  }

  override fun getPurchases(packageName: String, walletAddress: String, walletSignature: String,
                            type: BillingSupportedType): Single<List<Purchase>> {
    return if (type == BillingSupportedType.INAPP) {
      remoteRepository.getPurchases(packageName, walletAddress, walletSignature)
    } else {
      remoteRepository.getPurchasesSubs(packageName)
    }
  }

  override fun consumePurchases(packageName: String, purchaseToken: String, walletAddress: String,
                                walletSignature: String,
                                type: BillingSupportedType?): Single<Boolean> {
    return when (type) {
      null -> remoteRepository.consumePurchase(packageName, purchaseToken, walletAddress,
          walletSignature)
          .onErrorResumeNext { remoteRepository.consumePurchaseSubs(packageName) }

      BillingSupportedType.INAPP -> remoteRepository.consumePurchase(packageName, purchaseToken,
          walletAddress, walletSignature)

      else -> remoteRepository.consumePurchaseSubs(packageName)
    }
  }

  override fun getPaymentMethods(value: String?, currency: String?,
                                 type: String?,
                                 direct: Boolean?): Single<List<PaymentMethodEntity>> {
    return remoteRepository.getPaymentMethods(value, currency, type, direct)
  }

  override fun getAppcoinsTransaction(uid: String, address: String,
                                      signedContent: String): Single<Transaction> {
    return remoteRepository.getAppcoinsTransaction(uid, address, signedContent)
  }

}
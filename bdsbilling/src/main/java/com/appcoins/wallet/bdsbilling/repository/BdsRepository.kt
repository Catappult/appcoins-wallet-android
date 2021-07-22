package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.BillingRepository
import com.appcoins.wallet.bdsbilling.repository.entity.PaymentMethodEntity
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.billing.repository.entity.Product
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
                                          developerWallet: String, entityOemId: String?,
                                          entityDomainId: String?,
                                          origin: String, type: String, developerPayload: String?,
                                          callback: String?, orderReference: String?,
                                          referrerUrl: String?): Single<String> {
    return remoteRepository.registerAuthorizationProof(
        origin, type, entityOemId, entityDomainId, id, paymentType,
        walletAddress, walletSignature, productName, packageName, priceValue, developerWallet,
        developerPayload, callback, orderReference, referrerUrl, null
    )
        .map { it.uid }
  }

  override fun registerPaymentProof(paymentId: String, paymentType: String, walletAddress: String,
                                    signedData: String, paymentProof: String): Completable {
    return remoteRepository.registerPaymentProof(paymentId, paymentType, walletAddress, signedData,
        paymentProof)
  }

  override fun isSupported(packageName: String, type: BillingSupportedType): Single<Boolean> {
    return remoteRepository.isBillingSupported(packageName, type)
  }

  override fun getSkuDetails(packageName: String, skus: List<String>): Single<List<Product>> {
    return remoteRepository.getSkuDetails(packageName, skus)
  }

  override fun getSkuPurchase(packageName: String, skuId: String?, walletAddress: String,
                              walletSignature: String): Single<Purchase> {
    return remoteRepository.getSkuPurchase(packageName, skuId, walletAddress, walletSignature)

  }

  override fun getSkuTransaction(packageName: String, skuId: String?,
                                 transactionType: TransactionType, walletAddress: String,
                                 walletSignature: String): Single<Transaction> {
    return remoteRepository.getSkuTransaction(packageName, skuId, transactionType, walletAddress,
        walletSignature)
        .flatMap {
          if (it.items.isNotEmpty()) {
            return@flatMap Single.just(it.items[0])
          }
          return@flatMap Single.just(Transaction.notFound())
        }
  }

  override fun getPurchases(packageName: String, walletAddress: String, walletSignature: String,
                            type: BillingSupportedType): Single<List<Purchase>> {
    return remoteRepository.getPurchases(packageName, walletAddress, walletSignature, type)
  }

  override fun consumePurchases(packageName: String, purchaseToken: String, walletAddress: String,
                                walletSignature: String): Single<Boolean> {
    return remoteRepository.consumePurchase(packageName, purchaseToken, walletAddress,
        walletSignature)
  }

  override fun getPaymentMethods(value: String?,
                                 currency: String?,
                                 currencyType: String?,
                                 direct: Boolean?,
                                 transactionType: String?): Single<List<PaymentMethodEntity>> {
    return remoteRepository.getPaymentMethods(value, currency, currencyType,
        direct, transactionType)
        .onErrorReturn {
          it.printStackTrace()
          ArrayList()
        }
  }

  override fun getAppcoinsTransaction(uid: String, address: String,
                                      signedContent: String): Single<Transaction> {
    return remoteRepository.getAppcoinsTransaction(uid, address, signedContent)
  }

}
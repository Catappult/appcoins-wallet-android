package com.appcoins.wallet.billing.repository

import com.appcoins.wallet.billing.BillingThrowableCodeMapper
import com.appcoins.wallet.billing.Repository
import com.appcoins.wallet.billing.repository.entity.Gateway
import com.appcoins.wallet.billing.repository.entity.Product
import com.appcoins.wallet.billing.repository.entity.Purchase
import io.reactivex.Completable
import io.reactivex.Single

class BdsRepository(private val remoteRepository: RemoteRepository,
                             private val errorMapper: BillingThrowableCodeMapper) : Repository {

  override fun registerAuthorizationProof(id: String, paymentType: String,
                                          walletAddress: String,
                                          walletSignature: String,
                                          productName: String,
                                          packageName: String,
                                          developerWallet: String,
                                          storeWallet: String): Single<String> {
    return remoteRepository.registerAuthorizationProof(id, paymentType, walletAddress,
        walletSignature, productName, packageName, developerWallet, storeWallet).map { it.uid }
  }

  override fun registerPaymentProof(paymentId: String, paymentType: String, walletAddress: String,
                                    signedData: String, paymentProof: String): Completable {
    return remoteRepository.registerPaymentProof(paymentId, paymentType, walletAddress, signedData,
        paymentProof).toObservable().ignoreElements()
  }

  override fun isSupported(packageName: String, type: BillingSupportedType): Single<Boolean> {
    return remoteRepository.isBillingSupported(packageName, type)
  }

  override fun getSkuDetails(packageName: String, skus: List<String>,
                             type: Repository.BillingType): Single<List<Product>> {
    return remoteRepository.getSkuDetails(packageName, skus, type.name)
        .onErrorResumeNext {
          it.printStackTrace()
          Single.error(errorMapper.mapException(it))
        }
  }

  override fun getSkuPurchase(packageName: String, skuId: String, walletAddress: String,
                              walletSignature: String): Single<Purchase> {
    return remoteRepository.getSkuPurchase(packageName, skuId, walletAddress, walletSignature)

  }

  override fun getSkuTransactionStatus(packageName: String, skuId: String, walletAddress: String,
                                       walletSignature: String): Single<String> {
    return remoteRepository.getSkuTransaction(packageName, skuId, walletAddress, walletSignature).map { it.status }
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

  override fun getGateways(): Single<List<Gateway>> {
    return remoteRepository.getGateways()
  }

}
package com.appcoins.wallet.billing.repository

import com.appcoins.wallet.billing.BillingThrowableCodeMapper
import com.appcoins.wallet.billing.Repository
import com.appcoins.wallet.billing.repository.entity.Gateway
import com.appcoins.wallet.billing.repository.entity.Product
import com.appcoins.wallet.billing.repository.entity.Purchase
import com.appcoins.wallet.billing.repository.entity.Transaction
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.HttpException

class BdsRepository(private val remoteRepository: RemoteRepository,
                    private val errorMapper: BillingThrowableCodeMapper) : Repository {

  override fun registerAuthorizationProof(id: String, paymentType: String,
                                          walletAddress: String,
                                          walletSignature: String,
                                          productName: String,
                                          packageName: String,
                                          developerWallet: String,
                                          storeWallet: String,
                                          developerPayload: String?): Single<String> {
    return remoteRepository.registerAuthorizationProof(id, paymentType, walletAddress,
        walletSignature, productName, packageName, developerWallet, storeWallet, developerPayload)
        .map { it.uid }
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

  override fun getSkuTransaction(packageName: String, skuId: String, walletAddress: String,
                                 walletSignature: String): Single<Transaction> {
    return remoteRepository.getSkuTransaction(packageName, skuId, walletAddress, walletSignature)
        .onErrorResumeNext { mapError(it) }
  }

  private fun mapError(it: Throwable): Single<Transaction> {
    if (it is HttpException && it.code() == 404) {
      return Single.just(Transaction.notFound())
    }
    return Single.error(it)
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

  override fun getAppcoinsTransaction(uid: String, address: String,
                                      signedContent: String): Single<Transaction> {
    return remoteRepository.getAppcoinsTransaction(uid, address, signedContent)
  }

}
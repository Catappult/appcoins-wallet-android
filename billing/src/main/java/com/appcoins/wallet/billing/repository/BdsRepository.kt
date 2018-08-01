package com.appcoins.wallet.billing.repository

import com.appcoins.wallet.billing.BillingThrowableCodeMapper
import com.appcoins.wallet.billing.Repository
import com.appcoins.wallet.billing.repository.entity.Product
import com.appcoins.wallet.billing.repository.entity.Purchase
import io.reactivex.Single

internal class BdsRepository(private val remoteRepository: RemoteRepository,
                             private val errorMapper: BillingThrowableCodeMapper) : Repository {
  override fun registerProof(id: String, paymentType: String,
                             walletAddress: String,
                             walletSignature: String,
                             productName: String,
                             packageName: String,
                             developerWallet: String,
                             storeWallet: String): Single<String> {
    return remoteRepository.registerAuthorizationProof(id, paymentType, walletAddress,
        walletSignature, productName, packageName, developerWallet, storeWallet).map { it.uid }
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

  override fun getPurchases(packageName: String, walletAddress: String, walletSignature: String,
                            type: BillingSupportedType): Single<List<Purchase>> {
    return remoteRepository.getPurchases(packageName, walletAddress, walletSignature, type)
  }

  override fun consumePurchases(packageName: String, purchaseToken: String, walletAddress: String,
                                walletSignature: String): Single<Boolean> {
    return remoteRepository.consumePurchase(packageName, purchaseToken, walletAddress,
        walletSignature)
  }
}
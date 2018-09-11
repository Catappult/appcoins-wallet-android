package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.repository.BillingSupportedType
import com.appcoins.wallet.billing.repository.entity.Gateway
import com.appcoins.wallet.billing.repository.entity.Product
import com.appcoins.wallet.billing.repository.entity.Purchase
import com.appcoins.wallet.billing.repository.entity.Transaction
import io.reactivex.Completable
import io.reactivex.Single

interface Repository {

  fun isSupported(packageName: String, type: BillingSupportedType): Single<Boolean>

  fun getSkuDetails(packageName: String, skus: List<String>,
                    type: BillingType): Single<List<Product>>

  fun getSkuPurchase(packageName: String, skuId: String, walletAddress: String,
                     walletSignature: String): Single<Purchase>

  fun getSkuTransaction(packageName: String, skuId: String, walletAddress: String,
                        walletSignature: String): Single<Transaction>

  fun getPurchases(packageName: String, walletAddress: String, walletSignature: String,
                   type: BillingSupportedType): Single<List<Purchase>>

  fun consumePurchases(packageName: String, purchaseToken: String, walletAddress: String,
                       walletSignature: String): Single<Boolean>

  fun registerAuthorizationProof(id: String, paymentType: String, walletAddress: String,
                                 walletSignature: String, productName: String, packageName: String,
                                 developerWallet: String, storeWallet: String,
                                 developerPayload: String?): Single<String>

  fun registerPaymentProof(paymentId: String, paymentType: String, walletAddress: String,
                           signedData: String, paymentProof: String): Completable

  fun getGateways(): Single<List<Gateway>>

  fun getAppcoinsTransaction(uid: String, address: String,
                             signedContent: String): Single<Transaction>

  enum class BillingType {
    inapp, subs
  }


}

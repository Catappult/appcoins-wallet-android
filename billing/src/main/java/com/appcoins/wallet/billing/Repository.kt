package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.repository.BillingSupportedType
import com.appcoins.wallet.billing.repository.entity.Product
import com.appcoins.wallet.billing.repository.entity.Purchase
import io.reactivex.Single

internal interface Repository {

  fun isSupported(packageName: String, type: BillingSupportedType): Single<Boolean>

  fun getSkuDetails(packageName: String, skus: List<String>,
                    type: BillingType): Single<List<Product>>

  fun getPurchases(packageName: String, walletAddress: String, walletSignature: String,
                   type: BillingSupportedType): Single<List<Purchase>>

  fun consumePurchases(packageName: String, purchaseToken: String, walletAddress: String,
                       walletSignature: String): Single<Boolean>

  fun registerProof(id: String, paymentType: String, walletAddress: String,
                    walletSignature: String,
                    productName: String,
                    packageName: String,
                    developerWallet: String,
                    storeWallet: String): Single<String>

  enum class BillingType {
    inapp, subs
  }
}

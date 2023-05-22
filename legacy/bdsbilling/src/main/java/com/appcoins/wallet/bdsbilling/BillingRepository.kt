package com.appcoins.wallet.bdsbilling

import com.appcoins.wallet.bdsbilling.repository.entity.Product
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.core.network.microservices.model.BillingSupportedType
import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.network.microservices.model.Transaction
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigDecimal

interface BillingRepository {

  fun isSupported(packageName: String, type: BillingSupportedType): Single<Boolean>

  fun getSkuDetails(packageName: String, skus: List<String>,
                    type: BillingSupportedType): Single<List<Product>>

  fun getSkuPurchase(packageName: String, skuId: String?, purchaseUid: String?,
                     walletAddress: String, walletSignature: String,
                     type: BillingSupportedType): Single<Purchase>

  fun getSkuTransaction(packageName: String, skuId: String?, walletAddress: String,
                        walletSignature: String, type: BillingSupportedType): Single<Transaction>

  fun getPurchases(packageName: String, walletAddress: String, walletSignature: String,
                   type: BillingSupportedType): Single<List<Purchase>>

  fun consumePurchases(packageName: String, purchaseToken: String, walletAddress: String,
                       walletSignature: String, type: BillingSupportedType?): Single<Boolean>

  fun getSubscriptionToken(packageName: String, skuId: String, walletAddress: String,
                           walletSignature: String): Single<String>

  fun registerAuthorizationProof(id: String, paymentType: String, walletAddress: String,
                                 walletSignature: String, productName: String?, packageName: String,
                                 priceValue: BigDecimal, developerWallet: String,
                                 entityOemId: String?, entityDomainId: String?,
                                 origin: String, type: String, developerPayload: String?,
                                 callback: String?, orderReference: String?,
                                 referrerUrl: String?
  ): Single<Transaction>

  fun registerPaymentProof(
    paymentId: String, paymentType: String, walletAddress: String,
    signedData: String, paymentProof: String
  ): Completable

  fun getPaymentMethods(
    value: String? = null,
    currency: String? = null,
    currencyType: String? = null,
    direct: Boolean? = null,
    transactionType: String? = null,
    packageName: String? = null
  ): Single<List<PaymentMethodEntity>>

  fun getAppcoinsTransaction(
    uid: String, address: String,
    signedContent: String
  ): Single<Transaction>

  fun getWallet(packageName: String): Single<String>
}

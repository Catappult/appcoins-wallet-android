package com.appcoins.wallet.bdsbilling

import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.TransactionType
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType.Companion.isManagedType
import com.appcoins.wallet.bdsbilling.repository.entity.PaymentMethodEntity
import com.appcoins.wallet.bdsbilling.repository.entity.Product
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import io.reactivex.Scheduler
import io.reactivex.Single

class BdsBilling(private val repository: BillingRepository,
                 private val walletService: WalletService,
                 private val errorMapper: BillingThrowableCodeMapper) : Billing {
  override fun getWallet(packageName: String): Single<String> {
    return repository.getWallet(packageName)
  }

  override fun isInAppSupported(merchantName: String): Single<Billing.BillingSupportType> {
    return repository.isSupported(merchantName, BillingSupportedType.INAPP)
        .map { map(it) }
        .onErrorReturn { errorMapper.map(it) }
  }

  override fun isSubsSupported(merchantName: String): Single<Billing.BillingSupportType> {
    return repository.isSupported(merchantName, BillingSupportedType.INAPP_SUBSCRIPTION)
        .map { map(it) }
        .onErrorReturn { errorMapper.map(it) }
  }

  override fun getProducts(merchantName: String, skus: List<String>,
                           type: BillingSupportedType): Single<List<Product>> {
    return repository.getSkuDetails(merchantName, skus, type)
  }

  override fun getAppcoinsTransaction(uid: String, scheduler: Scheduler): Single<Transaction> {
    return walletService.getAndSignCurrentWalletAddress()
        .observeOn(scheduler)
        .flatMap { repository.getAppcoinsTransaction(uid, it.address, it.signedAddress) }
  }

  override fun getSkuTransaction(merchantName: String, sku: String?,
                                 scheduler: Scheduler,
                                 type: BillingSupportedType): Single<Transaction> {
    return walletService.getAndSignCurrentWalletAddress()
        .observeOn(scheduler)
        .flatMap {
          repository.getSkuTransaction(merchantName, sku, it.address, it.signedAddress, type)
        }
  }

  private fun mapTransactionType(transactionType: String): TransactionType {
    return if (transactionType.equals("INAPP_UNMANAGED", true)) {
      TransactionType.INAPP_UNMANAGED
    } else {
      TransactionType.INAPP
    }
  }

  override fun getSkuPurchase(merchantName: String, sku: String?, purchaseUid: String?,
                              scheduler: Scheduler, type: BillingSupportedType): Single<Purchase> {
    return walletService.getAndSignCurrentWalletAddress()
        .observeOn(scheduler)
        .flatMap {
          repository.getSkuPurchase(merchantName, sku, purchaseUid, it.address, it.signedAddress,
              type)
        }
  }

  override fun getPurchases(packageName: String, type: BillingSupportedType,
                            scheduler: Scheduler): Single<List<Purchase>> {
    return if (isManagedType(type)) {
      walletService.getAndSignCurrentWalletAddress()
          .observeOn(scheduler)
          .flatMap {
            repository.getPurchases(packageName, it.address, it.signedAddress, type)
          }
          .onErrorReturn { emptyList() }
    } else Single.just(emptyList())
  }

  override fun consumePurchases(merchantName: String, purchaseToken: String,
                                scheduler: Scheduler,
                                type: BillingSupportedType?): Single<Boolean> {
    return walletService.getAndSignCurrentWalletAddress()
        .observeOn(scheduler)
        .flatMap {
          repository.consumePurchases(merchantName, purchaseToken, it.address, it.signedAddress,
              type)
        }
  }

  override fun getPaymentMethods(value: String,
                                 currency: String,
                                 transactionType: String): Single<List<PaymentMethodEntity>> {
    return repository.getPaymentMethods(value, currency, transactionType = transactionType)
  }

  private fun map(it: Boolean) =
      if (it) Billing.BillingSupportType.SUPPORTED else Billing.BillingSupportType.MERCHANT_NOT_FOUND
}
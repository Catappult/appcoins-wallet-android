package com.appcoins.wallet.bdsbilling

import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
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
    return repository.isSupported(merchantName, BillingSupportedType.SUBS)
        .map { map(it) }
        .onErrorReturn { errorMapper.map(it) }
  }

  override fun getProducts(merchantName: String, skus: List<String>,
                           type: BillingSupportedType): Single<List<Product>> {
    return repository.getSkuDetails(merchantName, skus, type)
  }

  override fun getAppcoinsTransaction(uid: String, scheduler: Scheduler): Single<Transaction> {
    return walletService.getWalletAddress()
        .flatMap { address ->
          walletService.signContent(address)
              .observeOn(scheduler)
              .flatMap { signedContent ->
                repository.getAppcoinsTransaction(uid, address, signedContent)
              }
        }
  }

  override fun getSkuTransaction(merchantName: String, sku: String?,
                                 scheduler: Scheduler,
                                 type: BillingSupportedType): Single<Transaction> {
    return walletService.getWalletAddress()
        .flatMap { address ->
          walletService.signContent(address)
              .observeOn(scheduler)
              .flatMap { signedContent ->
                repository.getSkuTransaction(merchantName, sku, address, signedContent, type)
              }
        }
  }

  override fun getSkuPurchase(merchantName: String, sku: String?, scheduler: Scheduler,
                              type: BillingSupportedType): Single<Purchase> {
    return walletService.getWalletAddress()
        .flatMap { address ->
          walletService.signContent(address)
              .observeOn(scheduler)
              .flatMap { signedContent ->
                repository.getSkuPurchase(merchantName, sku, address, signedContent, type)
              }
        }
  }

  override fun getPurchases(merchantName: String, type: BillingSupportedType,
                            scheduler: Scheduler): Single<List<Purchase>> {
    return walletService.getWalletAddress()
        .flatMap { address ->
          walletService.signContent(address)
              .observeOn(scheduler)
              .flatMap { signedContent ->
                repository.getPurchases(merchantName, address, signedContent, type)
              }
        }
        .onErrorReturn { emptyList() }
  }

  override fun consumePurchases(merchantName: String, purchaseToken: String,
                                scheduler: Scheduler): Single<Boolean> {
    return walletService.getWalletAddress()
        .flatMap { address ->
          walletService.signContent(address)
              .observeOn(scheduler)
              .flatMap { signedContent ->
                repository.consumePurchases(merchantName, purchaseToken, address, signedContent)
              }
        }
  }

  override fun getPaymentMethods(value: String,
                                 currency: String): Single<List<PaymentMethodEntity>> {
    return repository.getPaymentMethods(value, currency)
        .onErrorReturn {
          it.printStackTrace()
          ArrayList()
        }
  }

  private fun map(it: Boolean) =
      if (it) Billing.BillingSupportType.SUPPORTED else Billing.BillingSupportType.MERCHANT_NOT_FOUND


}
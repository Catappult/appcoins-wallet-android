package com.appcoins.wallet.bdsbilling

import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.entity.PaymentMethod
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.billing.repository.entity.Product
import io.reactivex.Scheduler
import io.reactivex.Single

class BdsBilling(private val repository: BillingRepository,
                 private val walletService: WalletService,
                 private val errorMapper: BillingThrowableCodeMapper) : Billing {
  override fun getWallet(packageName: String): Single<String> {
    return repository.getWallet(packageName)
  }

  override fun isInAppSupported(merchantName: String): Single<Billing.BillingSupportType> {
    return repository.isSupported(merchantName, BillingSupportedType.INAPP).map { map(it) }
        .onErrorReturn { errorMapper.map(it) }
  }

  override fun isSubsSupported(merchantName: String): Single<Billing.BillingSupportType> {
    return repository.isSupported(merchantName, BillingSupportedType.SUBS).map { map(it) }
        .onErrorReturn { errorMapper.map(it) }
  }

  override fun getProducts(merchantName: String, skus: List<String>): Single<List<Product>> {
    return repository.getSkuDetails(merchantName, skus)
  }

  override fun getAppcoinsTransaction(uid: String,
                                      scheduler: Scheduler): Single<Transaction> {
    return walletService.getWalletAddress().flatMap { address ->
      walletService.signContent(address).observeOn(scheduler).flatMap { signedContent ->
        repository.getAppcoinsTransaction(uid, address, signedContent)
      }
    }
  }

  override fun getSkuTransaction(merchantName: String, sku: String,
                                 scheduler: Scheduler): Single<Transaction> {
    return walletService.getWalletAddress().flatMap { address ->
      walletService.signContent(address).observeOn(scheduler).flatMap { signedContent ->
        repository.getSkuTransaction(merchantName, sku, address, signedContent)
      }
    }
  }

  override fun getSkuPurchase(merchantName: String, sku: String,
                              scheduler: Scheduler): Single<Purchase> {
    return walletService.getWalletAddress().flatMap { address ->
      walletService.signContent(address).observeOn(scheduler).flatMap { signedContent ->
        repository.getSkuPurchase(merchantName, sku, address, signedContent)
      }
    }
  }

  override fun getPurchases(merchantName: String, type: BillingSupportedType,
                            scheduler: Scheduler): Single<List<Purchase>> {
    return walletService.getWalletAddress().flatMap { address ->
      walletService.signContent(address).observeOn(scheduler).flatMap { signedContent ->
        repository.getPurchases(merchantName, address, signedContent,
            type)
      }
    }.onErrorReturn { ArrayList() }
  }

  override fun consumePurchases(merchantName: String, purchaseToken: String,
                                scheduler: Scheduler): Single<Boolean> {
    return walletService.getWalletAddress().flatMap { address ->
      walletService.signContent(address).observeOn(scheduler).flatMap { signedContent ->
        repository.consumePurchases(merchantName, purchaseToken, address, signedContent)
      }
    }.onErrorReturn { false }
  }

  override fun getPaymentMethods(): Single<List<PaymentMethod>> {
    return repository.getPaymentMethods()
        .onErrorReturn {
          it.printStackTrace()
          ArrayList()
        }
  }

  private fun map(it: Boolean) =
      if (it) Billing.BillingSupportType.SUPPORTED else Billing.BillingSupportType.MERCHANT_NOT_FOUND


}
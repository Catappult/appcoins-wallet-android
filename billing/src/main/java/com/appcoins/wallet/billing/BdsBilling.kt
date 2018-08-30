package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.repository.BillingSupportedType
import com.appcoins.wallet.billing.repository.entity.Gateway
import com.appcoins.wallet.billing.repository.entity.Product
import com.appcoins.wallet.billing.repository.entity.Purchase
import com.appcoins.wallet.billing.repository.entity.Transaction
import io.reactivex.Scheduler
import io.reactivex.Single

class BdsBilling(private val merchantName: String,
                 private val repository: Repository,
                 private val walletService: WalletService,
                 private val errorMapper: BillingThrowableCodeMapper) : Billing {

  override fun isInAppSupported(): Single<Billing.BillingSupportType> {
    return repository.isSupported(merchantName, BillingSupportedType.INAPP).map { map(it) }
        .onErrorReturn { errorMapper.map(it) }
  }

  override fun isSubsSupported(): Single<Billing.BillingSupportType> {
    return repository.isSupported(merchantName, BillingSupportedType.SUBS).map { map(it) }
        .onErrorReturn { errorMapper.map(it) }
  }

  override fun getProducts(skus: List<String>, type: String): Single<List<Product>> {
    return repository.getSkuDetails(merchantName, skus, Repository.BillingType.valueOf(type))
  }

  override fun getAppcoinsTransaction(uid: String,
                                      scheduler: Scheduler): Single<Transaction> {
    return walletService.getWalletAddress().flatMap { address ->
      walletService.signContent(address).observeOn(scheduler).flatMap { signedContent ->
        repository.getAppcoinsTransaction(uid, address, signedContent)
      }
    }
  }

  override fun getSkuTransaction(sku: String, scheduler: Scheduler): Single<Transaction> {
    return walletService.getWalletAddress().flatMap { address ->
      walletService.signContent(address).observeOn(scheduler).flatMap { signedContent ->
        repository.getSkuTransaction(merchantName, sku, address, signedContent)
      }
    }
  }

  override fun getSkuPurchase(sku: String, scheduler: Scheduler): Single<Purchase> {
    return walletService.getWalletAddress().flatMap { address ->
      walletService.signContent(address).observeOn(scheduler).flatMap { signedContent ->
        repository.getSkuPurchase(merchantName, sku, address, signedContent)
      }
    }
  }

  override fun getPurchases(type: BillingSupportedType,
                            scheduler: Scheduler): Single<List<Purchase>> {
    return walletService.getWalletAddress().flatMap { address ->
      walletService.signContent(address).observeOn(scheduler).flatMap { signedContent ->
        repository.getPurchases(merchantName, address, signedContent,
            type)
      }
    }.onErrorReturn { ArrayList() }
  }

  override fun consumePurchases(purchaseToken: String, scheduler: Scheduler): Single<Boolean> {
    return walletService.getWalletAddress().flatMap { address ->
      walletService.signContent(address).observeOn(scheduler).flatMap { signedContent ->
        repository.consumePurchases(merchantName, purchaseToken, address, signedContent)
      }
    }.onErrorReturn { false }
  }

  override fun getGateways(): Single<List<Gateway>> {
    return repository.getGateways().map { it }
        .onErrorReturn { ArrayList() }
  }

  private fun map(it: Boolean) =
      if (it) Billing.BillingSupportType.SUPPORTED else Billing.BillingSupportType.MERCHANT_NOT_FOUND


}
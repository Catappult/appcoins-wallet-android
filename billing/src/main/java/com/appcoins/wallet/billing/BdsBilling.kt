package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.repository.BillingSupportedType
import com.appcoins.wallet.billing.repository.entity.Purchase
import com.appcoins.wallet.billing.repository.entity.Product
import com.google.gson.Gson
import io.reactivex.Single

internal class BdsBilling(private val merchantName: String,
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

  override fun getPurchases(type: BillingSupportedType): Single<List<Purchase>> {
    return walletService.getWalletAddress().flatMap { address ->
      walletService.signContent(address).flatMap { signedContent ->
        repository.getPurchases(merchantName, address, signedContent,
            type).map { it }
      }
    }.onErrorReturn { ArrayList() }
  }

  override fun consumePurchases(purchaseToken: String): Single<Boolean> {
    return walletService.getWalletAddress().flatMap { address ->
      walletService.signContent(address).flatMap { signedContent ->
        repository.consumePurchases(merchantName, purchaseToken, address, signedContent,
            Gson().toJson(Consume())).map { it }
      }
    }.onErrorReturn { false }
  }

  private fun map(it: Boolean) =
      if (it) Billing.BillingSupportType.SUPPORTED else Billing.BillingSupportType.MERCHANT_NOT_FOUND

  data class Consume(val status: String = "CONSUME")
}
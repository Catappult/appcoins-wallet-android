package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.repository.BillingSupportedType
import com.appcoins.wallet.billing.repository.entity.Product
import com.appcoins.wallet.billing.repository.entity.ProductsDetail
import io.reactivex.Single

internal class BdsBilling(private val repository: Repository,
                          private val errorMapper: BillingThrowableCodeMapper,
                          private val walletService: WalletService) : Billing {

  override fun isInAppSupported(packageName: String): Single<Billing.BillingSupportType> {
    return repository.isSupported(packageName, BillingSupportedType.INAPP).map { map(it) }
        .onErrorReturn { errorMapper.map(it) }
  }

  override fun isSubsSupported(packageName: String): Single<Billing.BillingSupportType> {
    return repository.isSupported(packageName, BillingSupportedType.SUBS).map { map(it) }
        .onErrorReturn { errorMapper.map(it) }
  }

  override fun getSkuDetails(packageName: String,
                             skuIds: List<String>, type: String): Single<ProductsDetail> {
    return walletService.getAddress().flatMap { walletAddress: String ->
      walletService.getSignature()
          .flatMap { signature: String ->
            repository.getSkuDetails(packageName, skuIds, Repository.BillingType.valueOf(type),
                walletAddress, signature).map { map(it) }
          }
    }
  }

  private fun map(products: List<Product>): ProductsDetail {
    return ProductsDetail(products)
  }

  private fun map(it: Boolean) =
      if (it) Billing.BillingSupportType.SUPPORTED else Billing.BillingSupportType.MERCHANT_NOT_FOUND
}
package com.appcoins.wallet.bdsbilling

import com.appcoins.wallet.bdsbilling.repository.entity.Product
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.core.analytics.analytics.partners.PartnerAddressService
import com.appcoins.wallet.core.network.microservices.model.BillingSupportedType
import com.appcoins.wallet.core.network.microservices.model.BillingSupportedType.Companion.isManagedType
import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.appcoins.wallet.core.walletservices.WalletService
import io.reactivex.Scheduler
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@BoundTo(supertype = Billing::class)
class BdsBilling @Inject constructor(
  private val repository: BillingRepository,
  private val walletService: WalletService,
  private val errorMapper: BillingThrowableCodeMapper,
  private val partnerAddressService: PartnerAddressService,
) : Billing {

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

  override fun getProducts(
    merchantName: String, skus: List<String>,
    type: BillingSupportedType
  ): Single<List<Product>> {
    return repository.getSkuDetails(merchantName, skus, type)
  }

  override fun getAppcoinsTransaction(uid: String, scheduler: Scheduler): Single<Transaction> {
    return walletService.getAndSignCurrentWalletAddress()
      .observeOn(scheduler)
      .flatMap { repository.getAppcoinsTransaction(uid, it.address, it.signedAddress) }
  }

  override fun getSkuTransaction(
    merchantName: String, sku: String?,
    scheduler: Scheduler,
    type: BillingSupportedType
  ): Single<Transaction> {
    return walletService.getAndSignCurrentWalletAddress()
      .observeOn(scheduler)
      .flatMap {
        repository.getSkuTransaction(merchantName, sku, it.address, it.signedAddress, type)
      }
  }

  override fun getSkuPurchase(
    merchantName: String, sku: String?, purchaseUid: String?,
    scheduler: Scheduler, type: BillingSupportedType
  ): Single<Purchase> {
    return walletService.getAndSignCurrentWalletAddress()
      .observeOn(scheduler)
      .flatMap {
        repository.getSkuPurchase(
          merchantName, sku, purchaseUid, it.address, it.signedAddress,
          type
        )
      }
  }

  override fun getPurchases(
    packageName: String, type: BillingSupportedType,
    scheduler: Scheduler
  ): Single<List<Purchase>> {
    return if (isManagedType(type)) {
      walletService.getAndSignCurrentWalletAddress()
        .observeOn(scheduler)
        .flatMap {
          repository.getPurchases(packageName, it.address, it.signedAddress, type)
        }
        .onErrorReturn { emptyList() }
    } else Single.just(emptyList())
  }

  override fun consumePurchases(
    merchantName: String, purchaseToken: String,
    scheduler: Scheduler,
    type: BillingSupportedType?
  ): Single<Boolean> {
    return repository.consumePurchases(merchantName, purchaseToken, type)
      .observeOn(scheduler)
  }

  override fun getSubscriptionToken(
    packageName: String, skuId: String,
    networkThread: Scheduler
  ): Single<String> {
    return walletService.getAndSignCurrentWalletAddress()
      .observeOn(networkThread)
      .flatMap {
        repository.getSubscriptionToken(packageName, skuId, it.address, it.signedAddress)
      }
  }

  override fun getPaymentMethods(
    value: String,
    currency: String,
    transactionType: String,
    packageName: String
  ): Single<List<PaymentMethodEntity>> {
    return partnerAddressService.getAttribution(packageName).flatMap { attributionEntity ->
      walletService.getWalletAddress()
        .flatMap { address ->
          repository.getPaymentMethods(
            value,
            currency,
            transactionType = transactionType,
            packageName = packageName,
            entityOemId = attributionEntity.oemId,
            address = address
          ).map { paymentMethods ->
            repository.replaceAppcPricesToOriginalPrices(paymentMethods, value, currency)
          }
        }
    }
  }

  private fun map(it: Boolean) =
    if (it) Billing.BillingSupportType.SUPPORTED else Billing.BillingSupportType.MERCHANT_NOT_FOUND
}
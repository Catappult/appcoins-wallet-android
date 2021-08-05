package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository
import com.appcoins.wallet.appcoins.rewards.Transaction
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.asfoundation.wallet.billing.partners.AddressService
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal

class RewardsManager(private val appcoinsRewards: AppcoinsRewards, private val billing: Billing,
                     private val partnerAddressService: AddressService) {

  val balance: Single<BigDecimal>
    get() = appcoinsRewards.getBalance()

  fun pay(sku: String?, amount: BigDecimal, developerAddress: String, packageName: String,
          origin: String?, type: String, payload: String?, callbackUrl: String?,
          orderReference: String?, referrerUrl: String?, productToken: String?): Completable {
    return partnerAddressService.getAttributionEntity(packageName)
        .flatMapCompletable { attrEntity ->
          appcoinsRewards.pay(
              amount, origin, sku, type, developerAddress, attrEntity.oemId, attrEntity.domain,
              packageName, payload, callbackUrl, orderReference, referrerUrl, productToken
          )
        }
  }

  fun getPaymentCompleted(packageName: String, sku: String?): Single<Purchase> {
    return billing.getSkuPurchase(packageName, sku, Schedulers.io())
  }

  fun getTransaction(packageName: String, sku: String?,
                     amount: BigDecimal): Observable<Transaction> {
    return appcoinsRewards.getPayment(packageName, sku, amount.toString())
  }

  fun getPaymentStatus(packageName: String, sku: String?,
                       amount: BigDecimal): Observable<RewardPayment> {
    return appcoinsRewards.getPayment(packageName, sku, amount.toString())
        .flatMap { this.map(it) }
  }

  private fun map(transaction: Transaction): Observable<RewardPayment> {
    return when (transaction.status) {
      Transaction.Status.PROCESSING -> Observable.just(
          RewardPayment(transaction.orderReference, Status.PROCESSING))
      Transaction.Status.COMPLETED -> Observable.just(
          RewardPayment(transaction.orderReference, Status.COMPLETED))
      Transaction.Status.ERROR -> Observable.just(
          RewardPayment(transaction.orderReference, Status.ERROR, transaction.errorCode,
              transaction.errorMessage))
      Transaction.Status.FORBIDDEN -> Observable.just(
          RewardPayment(transaction.orderReference, Status.FORBIDDEN))
      Transaction.Status.NO_NETWORK -> Observable.just(
          RewardPayment(transaction.orderReference, Status.NO_NETWORK))
      else -> throw UnsupportedOperationException(
          "Transaction status " + transaction.status + " not supported")
    }
  }

  fun sendCredits(toWallet: String, amount: BigDecimal,
                  packageName: String): Single<AppcoinsRewardsRepository.Status> {
    return appcoinsRewards.sendCredits(toWallet, amount, packageName)
  }
}
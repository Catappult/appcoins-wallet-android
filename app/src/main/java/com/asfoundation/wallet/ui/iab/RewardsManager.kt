package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository
import com.appcoins.wallet.appcoins.rewards.Transaction
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.core.network.microservices.model.BillingSupportedType
import com.appcoins.wallet.core.utils.partners.AddressService
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import javax.inject.Inject

class RewardsManager @Inject constructor(private val appcoinsRewards: AppcoinsRewards,
                                         private val billing: Billing,
                                         private val partnerAddressService: com.appcoins.wallet.core.utils.partners.AddressService
) {

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

  fun getPaymentCompleted(packageName: String, sku: String?, purchaseUid: String?,
                          billingType: BillingSupportedType): Single<Purchase> {
    return billing.getSkuPurchase(packageName, sku, purchaseUid, Schedulers.io(), billingType)
  }

  fun getTransaction(packageName: String, sku: String?,
                     amount: BigDecimal): Observable<Transaction> {
    return appcoinsRewards.getPayment(packageName, sku, amount.toString())
  }

  fun getPaymentStatus(packageName: String, sku: String?,
                       amount: BigDecimal): Observable<RewardPayment> {
    return appcoinsRewards.getPayment(packageName, sku, amount.toString())
        .flatMap { map(it) }
  }

  private fun map(transaction: Transaction): Observable<RewardPayment> {
    return when (transaction.status) {
      Transaction.Status.PROCESSING -> Observable.just(
          RewardPayment(transaction.orderReference, Status.PROCESSING))
      Transaction.Status.COMPLETED -> Observable.just(
          RewardPayment(transaction.orderReference, Status.COMPLETED, transaction.purchaseUid))
      Transaction.Status.ERROR -> Observable.just(
          RewardPayment(transaction.orderReference, Status.ERROR,
              errorCode = transaction.errorCode, errorMessage = transaction.errorMessage))
      Transaction.Status.FORBIDDEN -> Observable.just(
          RewardPayment(transaction.orderReference, Status.FORBIDDEN))
      Transaction.Status.SUB_ALREADY_OWNED -> Observable.just(
          RewardPayment(transaction.orderReference, Status.SUB_ALREADY_OWNED))
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
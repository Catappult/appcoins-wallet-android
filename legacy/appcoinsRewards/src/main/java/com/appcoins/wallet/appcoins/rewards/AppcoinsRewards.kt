package com.appcoins.wallet.appcoins.rewards

import com.appcoins.wallet.appcoins.rewards.repository.WalletService
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.core.utils.jvm_common.Repository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import com.appcoins.wallet.core.network.microservices.model.Transaction as CoreTransaction
import com.appcoins.wallet.core.network.microservices.model.Transaction.Status as CoreStatus

class AppcoinsRewards(
  private val repository: AppcoinsRewardsRepository,
  private val walletService: WalletService,
  private val cache: Repository<String, Transaction>,
  private val scheduler: Scheduler,
  private val billing: Billing,
  private val errorMapper: ErrorMapper
) {

  var lastPayTransaction: Transaction? = null

  fun pay(
    amount: BigDecimal, origin: String?, sku: String?, type: String,
    entityOemId: String?, entityDomainId: String?, packageName: String, payload: String?,
    callbackUrl: String?, orderReference: String?, referrerUrl: String?,
    productToken: String?, guestWalletId: String?
  ): Completable {
    lastPayTransaction = Transaction(
      sku = sku,
      type = type,
      developerAddress = "",
      entityOemId = entityOemId,
      entityDomain = entityDomainId,
      packageName = packageName,
      amount = amount,
      origin = origin,
      status = Transaction.Status.PENDING,
      txId = null,
      purchaseUid = null,
      payload = payload,
      callback = callbackUrl,
      orderReference = orderReference,
      referrerUrl = referrerUrl,
      guestWalletId = guestWalletId,
      productToken = productToken
    )
    return cache.save(getKey(amount.toString(), sku, packageName), lastPayTransaction)
  }

  fun start() {
    cache.all.observeOn(scheduler)
      .flatMapCompletable {
        Observable.fromIterable(it)
          .filter { transaction -> transaction.status == Transaction.Status.PENDING }
          .doOnNext { transaction ->
            cache.saveSync(
              getKey(transaction),
              Transaction(transaction, Transaction.Status.PROCESSING)
            )
          }
          .flatMapCompletable { transaction ->
            walletService.getWalletAddress()
              .flatMapCompletable { walletAddress ->
                walletService.signContent(walletAddress)
                  .flatMap { signature ->
                    repository.pay(
                      walletAddress = walletAddress,
                      signature = signature,
                      amount = transaction.amount,
                      origin = getOrigin(transaction),
                      sku = transaction.sku,
                      type = transaction.type,
                      entityOemId = transaction.entityOemId,
                      entityDomain = transaction.entityDomain,
                      packageName = transaction.packageName,
                      payload = transaction.payload,
                      callback = transaction.callback,
                      orderReference = transaction.orderReference,
                      referrerUrl = transaction.referrerUrl,
                      productToken = transaction.productToken,
                      guestWalletId = transaction.guestWalletId
                    )
                  }
                  .flatMapCompletable { transaction1 ->
                    waitTransactionCompletion(transaction1).andThen {
                      val tx = Transaction(transaction, Transaction.Status.COMPLETED)
                      tx.txId = transaction1.uid
                      tx.purchaseUid = transaction1.metadata?.purchaseUid
                      cache.saveSync(getKey(tx), tx)
                    }
                  }
              }
              .onErrorResumeNext { t ->
                t.printStackTrace()
                val error = errorMapper.map(t)
                val transactionStatus = mapToTransactionStatus(error.errorType)
                cache.save(
                  getKey(transaction),
                  Transaction(
                    transaction, transactionStatus, error.errorCode,
                    error.errorMessage
                  )
                )
              }
          }
      }
      .subscribe()
  }

  private fun mapToTransactionStatus(errorType: ErrorInfo.ErrorType): Transaction.Status {
    return when (errorType) {
      ErrorInfo.ErrorType.SUB_ALREADY_OWNED -> Transaction.Status.SUB_ALREADY_OWNED
      ErrorInfo.ErrorType.BLOCKED -> Transaction.Status.FORBIDDEN
      ErrorInfo.ErrorType.NO_NETWORK -> Transaction.Status.NO_NETWORK
      else -> Transaction.Status.ERROR
    }
  }

  private fun getOrigin(transaction: Transaction) =
    if (transaction.isBds()) transaction.origin else null

  private fun waitTransactionCompletion(
    createdTransaction: CoreTransaction
  ): Completable {
    return Observable.interval(0, 5, TimeUnit.SECONDS, scheduler)
      .timeInterval()
      .switchMap {
        billing.getAppcoinsTransaction(createdTransaction.uid, scheduler)
          .toObservable()
      }
      .takeUntil { pendingTransaction -> pendingTransaction.status != CoreStatus.PROCESSING }
      .ignoreElements()

  }

  fun getPayment(
    packageName: String, sku: String? = "",
    amount: String? = ""
  ): Observable<Transaction> =
    cache.get(getKey(amount, sku, packageName))
      .filter { it.status != Transaction.Status.PENDING }

  private fun getKey(transaction: Transaction): String =
    getKey(transaction.amount.toString(), transaction.sku, transaction.packageName)

  private fun getKey(amount: String? = "", sku: String? = "", packageName: String): String =
    amount + sku + packageName

  fun sendCredits(
    toWallet: String, amount: BigDecimal, currency: String,
    packageName: String, guestWalletId: String?
  ): Single<AppcoinsRewardsRepository.Status> {
    return walletService.getWalletAddress()
      .flatMap { walletAddress ->
        walletService.signContent(walletAddress)
          .flatMap { signature ->
            repository.sendCredits(
              toWallet, walletAddress, signature, amount, currency, "BDS",
              "TRANSFER", packageName, guestWalletId
            )
          }.map { statusAndTransaction ->
            statusAndTransaction.first
          }
      }
  }
}

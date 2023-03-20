package com.appcoins.wallet.appcoins.rewards

import com.appcoins.wallet.appcoins.rewards.repository.WalletService
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status
import com.appcoins.wallet.core.utils.jvm_common.Repository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class AppcoinsRewards(private val repository: AppcoinsRewardsRepository,
                      private val walletService: WalletService,
                      private val cache: Repository<String, Transaction>,
                      private val scheduler: Scheduler,
                      private val billing: Billing,
                      private val errorMapper: ErrorMapper) {

  fun pay(amount: BigDecimal, origin: String?, sku: String?, type: String, developerAddress: String,
          entityOemId: String?, entityDomainId: String?, packageName: String, payload: String?,
          callbackUrl: String?, orderReference: String?, referrerUrl: String?,
          productToken: String?): Completable {
    return cache.save(getKey(amount.toString(), sku, packageName),
        Transaction(
            sku, type, developerAddress, entityOemId, entityDomainId, packageName, amount,
            origin, Transaction.Status.PENDING, null, null, payload, callbackUrl, orderReference,
            referrerUrl, productToken
        ))
  }

  fun start() {
    cache.all.observeOn(scheduler)
        .flatMapCompletable {
          Observable.fromIterable(it)
              .filter { transaction -> transaction.status == Transaction.Status.PENDING }
              .doOnNext { transaction ->
                cache.saveSync(getKey(transaction),
                    Transaction(transaction, Transaction.Status.PROCESSING))
              }
              .flatMapCompletable { transaction ->
                walletService.getWalletAddress()
                    .flatMapCompletable { walletAddress ->
                      walletService.signContent(walletAddress)
                          .flatMap { signature ->
                            repository.pay(
                                walletAddress, signature, transaction.amount,
                                getOrigin(transaction), transaction.sku, transaction.type,
                                transaction.developerAddress, transaction.entityOemId,
                                transaction.entityDomain, transaction.packageName,
                                transaction.payload, transaction.callback,
                                transaction.orderReference, transaction.referrerUrl,
                                transaction.productToken
                            )
                          }
                          .flatMapCompletable { transaction1 ->
                            waitTransactionCompletion(transaction1).andThen {
                              val tx = Transaction(transaction, Transaction.Status.COMPLETED)
                              tx.txId = transaction1.hash
                              tx.purchaseUid = transaction1.metadata?.purchaseUid
                              cache.saveSync(getKey(tx), tx)
                            }
                          }
                    }
                    .onErrorResumeNext { t ->
                      t.printStackTrace()
                      val error = errorMapper.map(t)
                      val transactionStatus = mapToTransactionStatus(error.errorType)
                      cache.save(getKey(transaction),
                          Transaction(transaction, transactionStatus, error.errorCode,
                              error.errorMessage))
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
      createdTransaction: com.appcoins.wallet.bdsbilling.repository.entity.Transaction): Completable {
    return Observable.interval(0, 5, TimeUnit.SECONDS, scheduler)
        .timeInterval()
        .switchMap {
          billing.getAppcoinsTransaction(createdTransaction.uid, scheduler)
              .toObservable()
        }
        .takeUntil { pendingTransaction -> pendingTransaction.status != Status.PROCESSING }
        .ignoreElements()

  }

  fun getPayment(packageName: String, sku: String? = "",
                 amount: String? = ""): Observable<Transaction> =
      cache.get(getKey(amount, sku, packageName))
          .filter { it.status != Transaction.Status.PENDING }

  private fun getKey(transaction: Transaction): String =
      getKey(transaction.amount.toString(), transaction.sku, transaction.packageName)

  private fun getKey(amount: String? = "", sku: String? = "", packageName: String): String =
      amount + sku + packageName

  fun sendCredits(toWallet: String, amount: BigDecimal,
                  packageName: String): Single<AppcoinsRewardsRepository.Status> {
    return walletService.getWalletAddress()
        .flatMap { walletAddress ->
          walletService.signContent(walletAddress)
              .flatMap { signature ->
                repository.sendCredits(toWallet, walletAddress, signature, amount, "BDS",
                    "TRANSFER", packageName)
              }
        }
  }
}
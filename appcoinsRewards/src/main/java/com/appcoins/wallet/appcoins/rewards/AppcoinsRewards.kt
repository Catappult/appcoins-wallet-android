package com.appcoins.wallet.appcoins.rewards

import com.appcoins.wallet.appcoins.rewards.repository.WalletService
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status
import com.appcoins.wallet.commons.Repository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class AppcoinsRewards(
    private val repository: AppcoinsRewardsRepository,
    private val walletService: WalletService,
    private val cache: Repository<String, Transaction>,
    private val scheduler: Scheduler,
    private val billing: Billing,
    private val errorMapper: ErrorMapper) {

  fun getBalance(address: String): Single<BigDecimal> {
    return repository.getBalance(address)
  }

  fun getBalance(): Single<BigDecimal> {
    return walletService.getWalletAddress().flatMap { getBalance(it) }
  }

  fun pay(amount: BigDecimal,
          origin: Transaction.Origin, sku: String,
          type: String,
          developerAddress: String,
          storeAddress: String,
          oemAddress: String,
          packageName: String): Completable {
    return cache.save(getKey(sku, packageName),
        Transaction(sku, type, developerAddress, storeAddress, oemAddress, packageName, amount,
            origin, Transaction.Status.PENDING))
  }

  fun start() {
    cache.all.observeOn(scheduler).flatMapCompletable {
      Observable.fromIterable(it)
          .filter { transaction -> transaction.status == Transaction.Status.PENDING }
          .doOnNext { transaction ->
            cache.saveSync(getKey(transaction),
                Transaction(transaction, Transaction.Status.PROCESSING))
          }
          .flatMapCompletable { transaction ->
            walletService.getWalletAddress()
                .flatMapCompletable { walletAddress ->
                  walletService.signContent(walletAddress).flatMap { signature ->
                    repository.pay(walletAddress, signature, transaction.amount,
                        getOrigin(transaction),
                        transaction.sku,
                        transaction.type, transaction.developerAddress, transaction.storeAddress,
                        transaction.oemAddress, transaction.packageName)
                  }
                      .flatMapCompletable { createdTransaction ->
                        waitTransactionCompletion(createdTransaction)
                      }
                }.andThen(cache.save(getKey(transaction),
                    Transaction(transaction, Transaction.Status.COMPLETED)))
                .onErrorResumeNext {
                  it.printStackTrace()
                  cache.save(getKey(transaction),
                      Transaction(transaction, errorMapper.map(it)))
                }
          }
    }.subscribe()
  }

  private fun getOrigin(
      transaction: Transaction) =
      if (transaction.origin.isBds()) transaction.origin.name else null

  private fun waitTransactionCompletion(
      createdTransaction: com.appcoins.wallet.bdsbilling.repository.entity.Transaction): Completable {
    return Observable.interval(0, 5, TimeUnit.SECONDS, scheduler)
        .timeInterval()
        .switchMap {
          billing.getAppcoinsTransaction(createdTransaction.uid, scheduler).toObservable()
        }
        .takeUntil { pendingTransaction ->
          pendingTransaction.status != Status.PROCESSING
        }.ignoreElements()

  }

  fun getPayment(packageName: String, sku: String): Observable<Transaction> =
      cache.get(getKey(sku, packageName)).filter { it.status != Transaction.Status.PENDING }

  private fun getKey(transaction: Transaction): String =
      getKey(transaction.sku, transaction.packageName)

  private fun getKey(sku: String, packageName: String): String = sku + packageName
}
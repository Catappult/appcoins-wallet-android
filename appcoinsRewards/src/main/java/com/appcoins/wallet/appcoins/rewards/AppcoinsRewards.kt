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
          origin: String, sku: String?,
          type: String,
          developerAddress: String,
          storeAddress: String,
          oemAddress: String,
          packageName: String,
          payload: String?,
          callbackUrl: String?,
          orderReference: String?): Completable {
    return cache.save(getKey(amount.toString(), sku, packageName),
        Transaction(sku, type, developerAddress, storeAddress, oemAddress, packageName, amount,
            origin, Transaction.Status.PENDING, null, payload, callbackUrl, orderReference))
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
                        getOrigin(transaction), transaction.sku, transaction.type,
                        transaction.developerAddress, transaction.storeAddress,
                        transaction.oemAddress, transaction.packageName, transaction.payload,
                        transaction.callback, transaction.orderReference)

                  }
                      .flatMapCompletable { transaction1 ->
                        waitTransactionCompletion(transaction1).andThen {
                          val tx = Transaction(transaction, Transaction.Status.COMPLETED)
                          tx.txId = transaction1.hash
                          cache.saveSync(getKey(tx), tx)
                        }
                      }
                }
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
      if (transaction.isBds()) transaction.origin else null

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

  fun getPayment(packageName: String, sku: String? = "",
                 amount: String? = ""): Observable<Transaction> =
      cache.get(getKey(amount, sku, packageName)).filter { it.status != Transaction.Status.PENDING }

  private fun getKey(transaction: Transaction): String =
      getKey(transaction.amount.toString(), transaction.sku, transaction.packageName)

  private fun getKey(amount: String? = "", sku: String? = "", packageName: String): String =
      amount + sku + packageName

  fun sendCredits(toWallet: String, amount: BigDecimal,
                  packageName: String): Single<AppcoinsRewardsRepository.Status> {
    return walletService.getWalletAddress()
        .flatMap { walletAddress ->
          walletService.signContent(walletAddress).flatMap { signature ->
            repository.sendCredits(toWallet, walletAddress, signature, amount, "BDS", "TRANSFER",
                packageName)
          }
        }
  }
}
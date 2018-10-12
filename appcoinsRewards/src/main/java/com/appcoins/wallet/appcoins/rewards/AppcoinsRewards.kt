package com.appcoins.wallet.appcoins.rewards

import com.appcoins.wallet.appcoins.rewards.repository.WalletService
import com.appcoins.wallet.appcoins.rewards.repository.bds.Origin
import com.appcoins.wallet.appcoins.rewards.repository.bds.Type
import com.appcoins.wallet.commons.Repository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.math.BigDecimal

class AppcoinsRewards(
    private val repository: AppcoinsRewardsRepository,
    private val walletService: WalletService,
    private val cache: Repository<String, Transaction>,
    private val scheduler: Scheduler) {

  fun getBalance(address: String): Single<Long> {
    return repository.getBalance(address)
  }

  fun getBalance(): Single<Long> {
    return walletService.getWalletAddress().flatMap { getBalance(it) }
  }

  fun pay(amount: BigDecimal,
          origin: Origin, sku: String,
          type: Type,
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
                  walletService.signContent(walletAddress).flatMapCompletable { signature ->
                    repository.pay(walletAddress, signature, transaction.amount,
                        transaction.origin, transaction.sku,
                        transaction.type, transaction.developerAddress, transaction.storeAddress,
                        transaction.oemAddress, transaction.packageName)
                  }
                }.andThen(cache.save(getKey(transaction),
                    Transaction(transaction, Transaction.Status.COMPLETED)))
                .onErrorResumeNext {
                  it.printStackTrace()
                  cache.save(getKey(transaction),
                      Transaction(transaction, Transaction.Status.ERROR))
                }
          }
    }.subscribe()
  }

  fun getPayment(packageName: String, sku: String): Observable<Transaction> =
      cache.get(getKey(sku, packageName)).filter { it.status != Transaction.Status.PENDING }

  private fun getKey(transaction: Transaction): String =
      getKey(transaction.sku, transaction.packageName)

  private fun getKey(sku: String, packageName: String): String = sku + packageName
}
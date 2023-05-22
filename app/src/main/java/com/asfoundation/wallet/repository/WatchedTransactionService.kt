package com.asfoundation.wallet.repository

import com.appcoins.wallet.core.utils.jvm_common.Repository
import com.asfoundation.wallet.entity.TransactionBuilder
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class WatchedTransactionService(
  private val transactionSender: TransactionSender,
  private val cache: Repository<String, WatchedTransaction>,
  private val paymentErrorMapper: PaymentErrorMapper,
  private val scheduler: Scheduler,
  private val transactionTracker: TrackTransactionService
) {

  fun start() {
    cache.all
        .observeOn(scheduler)
      .flatMapCompletable { paymentTransactions ->
        Observable.fromIterable(paymentTransactions)
          .filter { transaction -> transaction.status == WatchedTransaction.Status.PENDING }
          .flatMapCompletable { executeTransaction(it) }
      }
      .doOnError { it.printStackTrace() }
      .retry()
      .subscribe()
  }

  private fun executeTransaction(watchedTransaction: WatchedTransaction): Completable {
    return cache.save(
      watchedTransaction.key,
      WatchedTransaction(
        watchedTransaction.key,
        WatchedTransaction.Status.PROCESSING,
        watchedTransaction.transactionBuilder
      )
    )
      .observeOn(scheduler)
      .andThen(
        transactionSender.send(watchedTransaction.transactionBuilder)
          .flatMapCompletable { hash ->
            cache.save(
              watchedTransaction.key,
              WatchedTransaction(
                watchedTransaction.key, WatchedTransaction.Status.PROCESSING,
                watchedTransaction.transactionBuilder, hash
              )
            )
              .andThen(transactionTracker.checkTransactionState(hash)
                .retryWhen { retryOnTransactionNotFound(it) }
                .ignoreElements()
                .andThen(
                  cache.save(
                    watchedTransaction.key,
                    WatchedTransaction(
                      watchedTransaction.key, WatchedTransaction.Status.COMPLETED,
                      watchedTransaction.transactionBuilder, hash
                    )
                  )
                ))
          })
        .doOnError {
          it.printStackTrace()
          cache.saveSync(
            watchedTransaction.key,
            WatchedTransaction(
              watchedTransaction.key, enumValueOf(paymentErrorMapper.map(it).paymentState.name),
              watchedTransaction.transactionBuilder
            )
          )
        }
  }

  private fun retryOnTransactionNotFound(throwable: Observable<Throwable>): Observable<Long> {
    return throwable.flatMap {
      if (it is TransactionNotFoundException) {
        Observable.timer(1, TimeUnit.SECONDS, Schedulers.trampoline())
      } else {
        Observable.error(it)
      }
    }
  }

  fun sendTransaction(key: String, transactionBuilder: TransactionBuilder): Completable {
    return cache.save(
      key,
      WatchedTransaction(key, WatchedTransaction.Status.PENDING, transactionBuilder)
    )
  }

  fun getTransaction(key: String): Observable<WatchedTransaction> =
    cache.get(key)
      .filter { it.status != WatchedTransaction.Status.PENDING }


  fun getAll(): Observable<List<WatchedTransaction>> =
    cache.all.flatMapSingle { transactions ->
      Observable.fromIterable(transactions)
        .filter { it.status != WatchedTransaction.Status.PENDING }
        .toList()
    }


  fun remove(key: String): Completable = cache.remove(key)

}

data class WatchedTransaction(
  val key: String,
  val status: Status,
  val transactionBuilder: TransactionBuilder,
  val transactionHash: String? = null
) {

  enum class Status {
    PENDING, PROCESSING, COMPLETED, ERROR, WRONG_NETWORK, NONCE_ERROR, UNKNOWN_TOKEN, NO_TOKENS,
    NO_ETHER, NO_FUNDS, NO_INTERNET, FORBIDDEN, SUB_ALREADY_OWNED
  }

}

interface TransactionSender {
  fun send(transactionBuilder: TransactionBuilder): Single<String>
}

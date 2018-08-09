package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.TransactionBuilder
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.math.BigInteger
import java.util.concurrent.TimeUnit

class WatchedTransactionService(private val transactionSender: TransactionSender,
                                private val cache: Repository<String, Transaction>,
                                private val errorMapper: ErrorMapper,
                                private val scheduler: Scheduler,
                                private val transactionTracker: TrackTransactionService) {

  fun start() {
    cache.all
        .observeOn(scheduler)
        .flatMapCompletable { paymentTransactions ->
          Observable.fromIterable(paymentTransactions)
              .filter { transaction ->
                transaction.status == Transaction.Status.PENDING
              }
              .flatMapCompletable { executeTransaction(it) }
        }
        .doOnError { it.printStackTrace() }
        .retry()
        .subscribe()
  }

  private fun executeTransaction(transaction: Transaction): Completable {
    return cache.save(transaction.key,
        Transaction(transaction.key, Transaction.Status.PROCESSING, transaction.transactionBuilder,
            transaction.nonce))
        .observeOn(scheduler)
        .andThen(transactionSender.send(transaction.transactionBuilder,
            transaction.nonce).flatMapCompletable { hash ->
          cache.save(transaction.key,
              Transaction(transaction.key, Transaction.Status.PROCESSING,
                  transaction.transactionBuilder,
                  transaction.nonce, hash))
              .andThen(transactionTracker.checkTransactionState(hash).retryWhen {
                retryOnTransactionNotFound(it)
              }.ignoreElements()
                  .andThen(cache.save(transaction.key,
                      Transaction(transaction.key, Transaction.Status.COMPLETED,
                          transaction.transactionBuilder,
                          transaction.nonce, hash))))
        })
        .doOnError { throwable ->
          throwable.printStackTrace()
          cache.save(transaction.key,
              Transaction(transaction.key, enumValueOf(errorMapper.map(throwable).name),
                  transaction.transactionBuilder, transaction.nonce, null))
        }
  }

  private fun retryOnTransactionNotFound(
      throwable: Observable<Throwable>): Observable<Long> {
    return throwable.flatMap {
      if (it is TransactionNotFoundException) {
        Observable.timer(1, TimeUnit.SECONDS, Schedulers.trampoline())
      } else {
        Observable.error(it)
      }
    }
  }

  fun sendTransaction(key: String, nonce: BigInteger,
                      transactionBuilder: TransactionBuilder): Completable {
    return cache.save(key, Transaction(key, Transaction.Status.PENDING, transactionBuilder, nonce))
  }

  fun getTransaction(key: String): Observable<Transaction> =
      cache.get(key).filter { it.status != Transaction.Status.PENDING }


  fun getAll(): Observable<List<Transaction>> =
      cache.all.flatMapSingle { transactions ->
        Observable.fromIterable(transactions).filter { it.status != Transaction.Status.PENDING }
            .toList()
      }


  fun remove(key: String): Completable {
    return cache.remove(key)
  }

}

data class Transaction(
    val key: String,
    val status: Status,
    val transactionBuilder: TransactionBuilder,
    val nonce: BigInteger, val transactionHash: String? = null) {

  enum class Status {
    PENDING, PROCESSING, COMPLETED, ERROR, WRONG_NETWORK, NONCE_ERROR, UNKNOWN_TOKEN, NO_TOKENS,
    NO_ETHER, NO_FUNDS, NO_INTERNET
  }

}

interface TransactionSender {
  fun send(transactionBuilder: TransactionBuilder,
           nonce: BigInteger): Single<String>
}

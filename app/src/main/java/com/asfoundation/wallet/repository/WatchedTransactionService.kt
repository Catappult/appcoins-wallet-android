package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.TransactionBuilder
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.math.BigInteger

internal class WatchedTransactionService(private val transactionSender: TransactionSender,
                                         private val cache: Repository<String, Transaction>,
                                         private val errorMapper: ErrorMapper,
                                         private val scheduler: Scheduler,
                                         private val transactionTracker: PendingTransactionService) {

  fun start() {
    cache.all
        .observeOn(scheduler)
        .flatMapCompletable { paymentTransactions ->
          Observable.fromIterable(paymentTransactions)
              .filter { transaction ->
                transaction.status == Status.PENDING
              }
              .flatMapCompletable { executeTransaction(it) }
        }
        .doOnError { it.printStackTrace() }
        .retry()
        .subscribe()
  }

  private fun executeTransaction(transaction: Transaction): Completable {
    return cache.save(transaction.key,
        Transaction(transaction.key, Status.PROCESSING, transaction.transactionBuilder,
            transaction.nonce))
        .observeOn(scheduler)
        .andThen(transactionSender.send(transaction.transactionBuilder,
            transaction.nonce).flatMapCompletable { hash ->
          cache.save(transaction.key,
              Transaction(transaction.key, Status.PROCESSING, transaction.transactionBuilder,
                  transaction.nonce, hash))
              .andThen(transactionTracker.checkTransactionState(hash).ignoreElements())
              .andThen(cache.save(transaction.key,
                  Transaction(transaction.key, Status.COMPLETED, transaction.transactionBuilder,
                      transaction.nonce, hash)))
        })
        .doOnError { throwable ->
          cache.save(transaction.key,
              Transaction(transaction.key, enumValueOf(errorMapper.map(throwable).name),
                  transaction.transactionBuilder, transaction.nonce, null))
        }
  }

  fun sendTransaction(key: String, paymentTransaction: PaymentTransaction,
                      nonce: BigInteger): Completable {
    return cache.save(key, Transaction(key, Status.PENDING, paymentTransaction.transactionBuilder,
        nonce))
  }

  fun getTransaction(key: String): Observable<Transaction> =
      cache.get(key).filter { it.status != Status.PENDING }


  fun getAll(): Observable<List<Transaction>> =
      cache.all.flatMapSingle { transactions ->
        Observable.fromIterable(transactions).filter { it.status != Status.PENDING }.toList()
      }


  fun remove(key: String): Completable {
    return cache.remove(key)
  }

}

enum class Status {
  PENDING, PROCESSING, COMPLETED, ERROR, WRONG_NETWORK, NONCE_ERROR, UNKNOWN_TOKEN, NO_TOKENS,
  NO_ETHER, NO_FUNDS, NO_INTERNET
}

data class Transaction(
    val key: String,
    val status: Status,
    val transactionBuilder: TransactionBuilder,
    val nonce: BigInteger, val transactionHash: String? = null)

interface TransactionSender {
  fun send(transactionBuilder: TransactionBuilder,
           nonce: BigInteger): Single<String>
}

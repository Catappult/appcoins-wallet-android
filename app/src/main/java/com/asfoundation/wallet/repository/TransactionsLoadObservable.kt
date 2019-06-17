package com.asfoundation.wallet.repository

import com.asfoundation.wallet.transactions.Transaction
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.concurrent.atomic.AtomicBoolean

class TransactionsLoadObservable(private val offChainTransactions: OffChainTransactions,
                                 private val wallet: String,
                                 private val startingDate: Long? = null,
                                 private val endDate: Long? = null,
                                 private val sort: OffChainTransactions.Sort? = null,
                                 private val limit: Int = 10) :
    Observable<List<Transaction>>() {

  override fun subscribeActual(observer: Observer<in List<Transaction>>) {
    val transactionDisposable = TransactionsDisposable()
    observer.onSubscribe(transactionDisposable)
    try {
      var i = 0
      var list: List<Transaction>? = null
      while (!transactionDisposable.isDisposed && (list == null || list.isNotEmpty())) {
        list = offChainTransactions.getTransactions(wallet, startingDate, endDate, i, sort, limit)
        if (!transactionDisposable.isDisposed) {
          observer.onNext(list)
        }
        i += limit
      }
      observer.onComplete()
    } catch (ex: Exception) {
      observer.onError(ex)
    }
  }

  inner class TransactionsDisposable : Disposable {
    private val unsubscribed = AtomicBoolean()
    override fun isDisposed(): Boolean {
      return unsubscribed.get()
    }

    override fun dispose() {
      unsubscribed.compareAndSet(false, true)
    }
  }
}
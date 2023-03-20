package com.asfoundation.wallet.repository

import com.appcoins.wallet.core.network.backend.model.WalletHistory
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
    Observable<List<WalletHistory.Transaction>>() {

  override fun subscribeActual(observer: Observer<in List<WalletHistory.Transaction>>) {
    val transactionDisposable = TransactionsDisposable()
    observer.onSubscribe(transactionDisposable)
    try {
      var i = 0
      var list: List<WalletHistory.Transaction>? = null
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
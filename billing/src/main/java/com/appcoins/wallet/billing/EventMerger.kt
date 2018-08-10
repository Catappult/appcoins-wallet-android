package com.appcoins.wallet.billing

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

class EventMerger<T>(private val publisher: PublishSubject<T>,
                     private val disposables: CompositeDisposable) {


  fun getEvents(): Observable<T> {
    return publisher
  }

  fun addSource(source: Observable<T>) {
    disposables.add(source.subscribe { publisher.onNext(it) })
  }

  fun stop() {
    disposables.clear()
  }
}

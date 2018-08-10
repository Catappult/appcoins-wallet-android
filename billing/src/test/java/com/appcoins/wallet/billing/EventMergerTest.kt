package com.appcoins.wallet.billing

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test

class EventMergerTest {
  lateinit var eventMerger: EventMerger<Int>
  lateinit var publishSubject: PublishSubject<Int>


  @Before
  fun setUp() {
    publishSubject = PublishSubject.create<Int>()
    eventMerger = EventMerger(PublishSubject.create(), CompositeDisposable())
  }

  @Test
  fun addSource() {
    eventMerger.addSource(publishSubject)
    val source = PublishSubject.create<Int>()
    eventMerger.addSource(source)
    val testObserver = TestObserver<Int>()
    eventMerger.getEvents().subscribe(testObserver)
    publishSubject.onNext(2)
    source.onNext(3)
    testObserver.assertValues(2, 3)
    testObserver.assertNoErrors()
  }

  @Test
  fun getEvents() {
    eventMerger.addSource(publishSubject)
    val testObserver = TestObserver<Int>()
    eventMerger.getEvents().subscribe(testObserver)
    publishSubject.onNext(2)
    testObserver.assertValue(2)
    testObserver.assertNoErrors()
  }

  @Test
  fun stop() {
    eventMerger.addSource(publishSubject)
    val testObserver = TestObserver<Int>()
    eventMerger.getEvents().subscribe(testObserver)
    eventMerger.stop()
    publishSubject.onNext(2)
    testObserver.assertNoValues()
    testObserver.assertNoErrors()
  }
}
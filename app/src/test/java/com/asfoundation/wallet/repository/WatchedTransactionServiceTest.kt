package com.asfoundation.wallet.repository

import com.appcoins.wallet.commons.MemoryCache
import com.asfoundation.wallet.entity.PendingTransaction
import com.asfoundation.wallet.entity.TransactionBuilder
import com.google.gson.Gson
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

@RunWith(MockitoJUnitRunner::class)
class WatchedTransactionServiceTest {

  @Mock
  lateinit var pendingTransactionService: PendingTransactionService

  @Mock
  lateinit var transactionSender: TransactionSender

  private lateinit var watchedTransactionService: WatchedTransactionService
  private lateinit var scheduler: TestScheduler

  private lateinit var transactionBuilder: TransactionBuilder
  private lateinit var pendingTransactionState: PublishSubject<PendingTransaction>
  private lateinit var nonce: BigInteger

  private val transactionHash = "hash"

  @Before
  fun setUp() {
    pendingTransactionState = PublishSubject.create<PendingTransaction>()

    `when`(pendingTransactionService.checkTransactionState(transactionHash)).thenReturn(
        pendingTransactionState)

    transactionBuilder = TransactionBuilder("APPC")
    nonce = BigInteger.ONE
    `when`(transactionSender.send(transactionBuilder)).thenReturn(Single.just(
        transactionHash))

    scheduler = TestScheduler()
    watchedTransactionService = WatchedTransactionService(transactionSender,
      MemoryCache(
        BehaviorSubject.create(),
        ConcurrentHashMap()
      ),
        PaymentErrorMapper(Gson()), scheduler, pendingTransactionService)
  }

  @Test
  fun start() {
  }

  @Test
  fun sendTransaction() {
    watchedTransactionService.start()
    scheduler.triggerActions()

    val uri = "uri"
    val observer = TestObserver<Transaction>()
    watchedTransactionService.getTransaction(uri)
        .subscribe(observer)
    scheduler.triggerActions()

    watchedTransactionService.sendTransaction(uri, transactionBuilder)
        .subscribe()

    scheduler.triggerActions()
    pendingTransactionState.onNext(PendingTransaction(transactionHash, true))
    scheduler.triggerActions()
    pendingTransactionState.onNext(PendingTransaction(transactionHash, true))
    scheduler.triggerActions()
    pendingTransactionState.onNext(PendingTransaction(transactionHash, false))
    scheduler.triggerActions()
    pendingTransactionState.onComplete()

    observer.assertValues(
        Transaction(uri, Transaction.Status.PROCESSING, transactionBuilder),
        Transaction(uri, Transaction.Status.PROCESSING, transactionBuilder, transactionHash),
        Transaction(uri, Transaction.Status.COMPLETED, transactionBuilder, transactionHash))
    observer.assertNoErrors()
  }
}
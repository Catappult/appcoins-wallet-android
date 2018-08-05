package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.PendingTransaction
import com.asfoundation.wallet.entity.TransactionBuilder
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

  val PACKAGE_NAME = "package_name"
  val PRODUCT_NAME = "product_name"
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
    `when`(transactionSender.send(transactionBuilder, nonce)).thenReturn(Single.just(
        transactionHash))

    scheduler = TestScheduler()
    watchedTransactionService = WatchedTransactionService(transactionSender,
        MemoryCache(BehaviorSubject.create(), ConcurrentHashMap()),
        ErrorMapper(), scheduler, pendingTransactionService)
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

    watchedTransactionService.sendTransaction(uri,
        PaymentTransaction(uri, transactionBuilder, PACKAGE_NAME, PRODUCT_NAME), nonce)
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
        Transaction(uri, Status.PROCESSING, transactionBuilder, nonce),
        Transaction(uri, Status.PROCESSING, transactionBuilder, nonce, transactionHash),
        Transaction(uri, Status.COMPLETED, transactionBuilder, nonce, transactionHash))
    observer.assertNoErrors()
  }
}
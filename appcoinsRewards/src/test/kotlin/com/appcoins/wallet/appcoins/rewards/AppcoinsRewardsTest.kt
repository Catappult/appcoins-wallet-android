package com.appcoins.wallet.appcoins.rewards

import com.appcoins.wallet.appcoins.rewards.repository.BdsAppcoinsRewardsRepository
import com.appcoins.wallet.appcoins.rewards.repository.BdsRemoteApi
import com.appcoins.wallet.appcoins.rewards.repository.WalletService
import com.appcoins.wallet.appcoins.rewards.repository.backend.BackendApi
import com.appcoins.wallet.appcoins.rewards.repository.bds.BdsApi
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.repository.GetTransactionIdResponse
import com.appcoins.wallet.bdsbilling.repository.entity.Gateway
import com.appcoins.wallet.commons.MemoryCache
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class AppcoinsRewardsTest {
  companion object {
    private const val USER_ADDRESS: String = "0xd9BA3c6932a5084D0CA0769893353D60b23AAfC4"
    private const val DEVELOPER_ADDRESS: String = "0x652d25ac09f79e9619fba99f34f0d8420d0956b1"
    private const val OEM_ADDRESS: String = "0x652d25ac09f79e9619fba99f34f0d8420d0956b1"
    private const val STORE_ADDRESS: String = "0x652d25ac09f79e9619fba99f34f0d8420d0956b1"
    private const val SKU: String = "cm.aptoide.pt:gas"
    private val BALANCE: BigDecimal = BigDecimal(2)
    private const val TYPE: String = "INAPP"
    private const val PACKAGE_NAME = "PACKAGE_NAME"
    private val ORIGIN = Transaction.Origin.BDS
    private val PRICE = BigDecimal("1700000000000000000")
    private const val UID = "UID"

  }

  private lateinit var appcoinsRewards: AppcoinsRewards

  private val scheduler = TestScheduler()
  @Mock
  lateinit var billing: Billing

  @Before
  fun setUp() {
    val api = object : BackendApi {
      override fun getBalance(address: String): Single<BackendApi.RewardBalanceResponse> {
        return Single.just(BackendApi.RewardBalanceResponse(BALANCE))
      }
    }

    val bdsApi = object : BdsApi {

      override fun pay(walletAddress: String, signature: String,
                       payBody: BdsApi.PayBody): Single<com.appcoins.wallet.bdsbilling.repository.entity.Transaction> {
        return Single.just(com.appcoins.wallet.bdsbilling.repository.entity.Transaction(UID,
            com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.COMPLETED,
                Gateway.unknown(), "0x32453134"))
      }
    }

    `when`(billing.getAppcoinsTransaction(UID, scheduler)).thenReturn(
        Single.just(com.appcoins.wallet.bdsbilling.repository.entity.Transaction(UID,
            com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.COMPLETED,
                Gateway.unknown(), "0x32453134")))

      val transactionIdRepositoryApi = Mockito.mock(TransactionIdRepository.Api::class.java)
      val transactionIdRepository = TransactionIdRepository(transactionIdRepositoryApi)
      val getTransactionIdResponse = GetTransactionIdResponse()
      getTransactionIdResponse.status = "PENDING"
      getTransactionIdResponse.txid = "0x32453134"

      `when`(transactionIdRepositoryApi.getTransactionId(ArgumentMatchers.anyString())).thenReturn(Single.just(getTransactionIdResponse))

      scheduler.advanceTimeBy(1, TimeUnit.DAYS)
      scheduler.triggerActions()

    appcoinsRewards =
        AppcoinsRewards(
            BdsAppcoinsRewardsRepository(BdsRemoteApi(api, bdsApi)), object : WalletService {
          override fun getWalletAddress(): Single<String> {
            return Single.just("0xd9BA3c6932a5084D0CA0769893353D60b23AAfC4")
          }

          override fun signContent(content: String): Single<String> {
            return Single.just(
                "27c3217155834a21fa8f97df99053f2874727837c03805c2eb1ba56383473b2a07fd865dd5db1359a717dfec9aa14bab6437184b14969ec3551b86e9d29c98d401")

          }
        }, MemoryCache(BehaviorSubject.create(), ConcurrentHashMap()), scheduler, billing
                , ErrorMapper(), transactionIdRepository)
    appcoinsRewards.start()
  }

  @Test
  fun makePayment() {
    val testObserver = TestObserver<Any>()
    appcoinsRewards.pay(PRICE,
        ORIGIN,
        SKU, TYPE, DEVELOPER_ADDRESS,
        STORE_ADDRESS,
        OEM_ADDRESS,
        PACKAGE_NAME).subscribe(testObserver)
    val statusObserver = TestObserver<Transaction>()
    appcoinsRewards.getPayment(PACKAGE_NAME, SKU).subscribe(statusObserver)

    scheduler.triggerActions()
    testObserver.assertNoErrors().assertComplete()
    val mutableListOf = mutableListOf(
        Transaction(SKU, TYPE, DEVELOPER_ADDRESS, STORE_ADDRESS, OEM_ADDRESS, PACKAGE_NAME,
                PRICE, ORIGIN, Transaction.Status.PROCESSING, null),
        Transaction(SKU, TYPE, DEVELOPER_ADDRESS, STORE_ADDRESS, OEM_ADDRESS, PACKAGE_NAME,
                PRICE, ORIGIN, Transaction.Status.COMPLETED, "0x32453134"))
    statusObserver.assertNoErrors().assertValueSequence(mutableListOf)
  }

  @Test
  fun getBalance() {
    val testObserverNoAddress = TestObserver<BigDecimal>()
    appcoinsRewards.getBalance().subscribe(testObserverNoAddress)
    testObserverNoAddress.assertNoErrors().assertValue(BALANCE).assertComplete()

    val testObserverWithAddress = TestObserver<BigDecimal>()
    appcoinsRewards.getBalance(USER_ADDRESS).subscribe(testObserverWithAddress)
    testObserverWithAddress.assertNoErrors().assertValue(BALANCE).assertComplete()
  }
}
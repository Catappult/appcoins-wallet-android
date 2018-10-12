package com.appcoins.wallet.appcoins.rewards

import com.appcoins.wallet.appcoins.rewards.repository.BdsAppcoinsRewardsRepository
import com.appcoins.wallet.appcoins.rewards.repository.BdsRemoteApi
import com.appcoins.wallet.appcoins.rewards.repository.WalletService
import com.appcoins.wallet.appcoins.rewards.repository.backend.BackendApi
import com.appcoins.wallet.appcoins.rewards.repository.bds.BdsApi
import com.appcoins.wallet.appcoins.rewards.repository.bds.Origin
import com.appcoins.wallet.appcoins.rewards.repository.bds.Type
import com.appcoins.wallet.commons.MemoryCache
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap

class AppcoinsRewardsTest {
  companion object {
    private const val USER_ADDRESS: String = "0xd9BA3c6932a5084D0CA0769893353D60b23AAfC4"
    private const val DEVELOPER_ADDRESS: String = "0x652d25ac09f79e9619fba99f34f0d8420d0956b1"
    private const val OEM_ADDRESS: String = "0x652d25ac09f79e9619fba99f34f0d8420d0956b1"
    private const val STORE_ADDRESS: String = "0x652d25ac09f79e9619fba99f34f0d8420d0956b1"
    private const val SKU: String = "cm.aptoide.pt:gas"
    private const val BALANCE: Long = 2
    private val TYPE: Type = Type.INAPP
    private const val PACKAGE_NAME = "PACKAGE_NAME"
    private val ORIGIN = Origin.BDS
    private val PRICE = BigDecimal("1700000000000000000")

  }

  private lateinit var appcoinsRewards: AppcoinsRewards

  private val scheduler = TestScheduler()

  @Before
  fun setUp() {
    val api = object : BackendApi {
      override fun getBalance(address: String): Single<BackendApi.RewardBalanceResponse> {
        return Single.just(BackendApi.RewardBalanceResponse(BALANCE))
      }
    }

    val bdsApi = object : BdsApi {
      override fun pay(walletAddress: String, signature: String,
                       payBody: BdsApi.PayBody): Completable {
        return Completable.complete()
      }
    }

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
        }, MemoryCache(BehaviorSubject.create(), ConcurrentHashMap()), scheduler)
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
            PRICE, ORIGIN, Transaction.Status.PROCESSING),
        Transaction(SKU, TYPE, DEVELOPER_ADDRESS, STORE_ADDRESS, OEM_ADDRESS, PACKAGE_NAME,
            PRICE, ORIGIN, Transaction.Status.COMPLETED))
    statusObserver.assertNoErrors().assertValueSequence(mutableListOf)
  }

  @Test
  fun getBalance() {
    val testObserverNoAddress = TestObserver<Long>()
    appcoinsRewards.getBalance().subscribe(testObserverNoAddress)
    testObserverNoAddress.assertNoErrors().assertValue(BALANCE).assertComplete()

    val testObserverWithAddress = TestObserver<Long>()
    appcoinsRewards.getBalance(USER_ADDRESS).subscribe(testObserverWithAddress)
    testObserverWithAddress.assertNoErrors().assertValue(BALANCE).assertComplete()
  }
}
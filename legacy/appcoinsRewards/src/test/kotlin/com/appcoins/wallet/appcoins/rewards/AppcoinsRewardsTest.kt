package com.appcoins.wallet.appcoins.rewards

import com.appcoins.wallet.appcoins.rewards.repository.BdsAppcoinsRewardsRepository
import com.appcoins.wallet.appcoins.rewards.repository.RemoteRepository
import com.appcoins.wallet.appcoins.rewards.repository.WalletService
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.commons.MemoryCache
import com.appcoins.wallet.core.network.microservices.model.Gateway
import com.appcoins.wallet.core.utils.jvm_common.MemoryCache
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.HttpException
import retrofit2.Response
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class AppcoinsRewardsTest {
  companion object {
    private const val USER_ADDRESS: String = "0xd9BA3c6932a5084D0CA0769893353D60b23AAfC4"
    private const val USER_ADDRESS_SIGNATURE: String =
        "27c3217155834a21fa8f97df99053f2874727837c03805c2eb1ba56383473b2a07fd865dd5db1359a717dfec9aa14bab6437184b14969ec3551b86e9d29c98d401"
    private const val DEVELOPER_ADDRESS: String = "0x652d25ac09f79e9619fba99f34f0d8420d0956b1"
    private const val OEM_ADDRESS: String = "0x652d25ac09f79e9619fba99f34f0d8420d0956b1"
    private const val STORE_ADDRESS: String = "0x652d25ac09f79e9619fba99f34f0d8420d0956b1"
    private const val SKU: String = "cm.aptoide.pt:gas"
    private const val PURCHASE_UID = "ve43f95meo"
    private val BALANCE: BigDecimal = BigDecimal(2)
    private const val TYPE: String = "INAPP"
    private const val TYPE_TRANSFER: String = "TRANSFER"
    private const val PACKAGE_NAME = "PACKAGE_NAME"
    private const val BDS_ORIGIN = "BDS"
    private const val UNITY_ORIGIN = "UNITY"
    private const val UNKNOWN_ORIGIN = "unknown"
    private val PRICE = BigDecimal("1700000000000000000")
    private const val UID = "UID"

  }

  private lateinit var appcoinsRewards: AppcoinsRewards

  private val scheduler = TestScheduler()

  @Mock
  lateinit var billing: Billing

  @Mock
  lateinit var remoteApi: RemoteRepository

  @Before
  fun setUp() {
    `when`(remoteApi.sendCredits(DEVELOPER_ADDRESS, USER_ADDRESS, USER_ADDRESS_SIGNATURE, PRICE,
        BDS_ORIGIN, TYPE_TRANSFER, PACKAGE_NAME)).thenReturn(Completable.complete())

    `when`(remoteApi.pay(USER_ADDRESS, USER_ADDRESS_SIGNATURE, PRICE, BDS_ORIGIN, SKU, TYPE,
        DEVELOPER_ADDRESS, STORE_ADDRESS, OEM_ADDRESS, PACKAGE_NAME, null, null, null,
        null,
        null)).thenReturn(
        Single.just(com.appcoins.wallet.bdsbilling.repository.entity.Transaction(UID,
            com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.COMPLETED,
            Gateway.unknown(), "0x32453134", null, "orderReference", null, "", null)))

    `when`(remoteApi.pay(USER_ADDRESS, USER_ADDRESS_SIGNATURE, PRICE, UNITY_ORIGIN, SKU, TYPE,
        DEVELOPER_ADDRESS, STORE_ADDRESS, OEM_ADDRESS, PACKAGE_NAME, null, null, null,
        null,
        null)).thenReturn(
        Single.just(com.appcoins.wallet.bdsbilling.repository.entity.Transaction(UID,
            com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.COMPLETED,
            Gateway.unknown(), "0x32453134", null, "orderReference", null, "", null)))

    `when`(remoteApi.pay(USER_ADDRESS, USER_ADDRESS_SIGNATURE, PRICE, null, SKU, TYPE,
        DEVELOPER_ADDRESS, STORE_ADDRESS, OEM_ADDRESS, PACKAGE_NAME, null, null, null,
        null,
        null)).thenReturn(
        Single.just(com.appcoins.wallet.bdsbilling.repository.entity.Transaction(UID,
            com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.COMPLETED,
            Gateway.unknown(), "0x32453134", null, "orderReference", null, "", null)))

    `when`(billing.getAppcoinsTransaction(UID, scheduler)).thenReturn(
        Single.just(com.appcoins.wallet.bdsbilling.repository.entity.Transaction(UID,
            com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.COMPLETED,
            Gateway.unknown(), "0x32453134", null, "orderReference", null, "", null)))

    scheduler.advanceTimeBy(1, TimeUnit.DAYS)
    scheduler.triggerActions()

    appcoinsRewards =
        AppcoinsRewards(
            BdsAppcoinsRewardsRepository(remoteApi), object : WalletService {
          override fun getWalletAddress(): Single<String> {
            return Single.just(USER_ADDRESS)
          }

          override fun signContent(content: String): Single<String> {
            return Single.just(USER_ADDRESS_SIGNATURE)

          }
        },
          MemoryCache(
            BehaviorSubject.create(),
            ConcurrentHashMap()
          ), scheduler, billing
            , ErrorMapper(Gson()))
    appcoinsRewards.start()
  }

  @Test
  fun makePayment() {
    val testObserver = TestObserver<Any>()
    appcoinsRewards.pay(
        PRICE, BDS_ORIGIN, SKU, TYPE, DEVELOPER_ADDRESS, STORE_ADDRESS, OEM_ADDRESS,
        PACKAGE_NAME, null, null, null, null, null
    )
        .subscribe(testObserver)
    val statusObserver = TestObserver<Transaction>()
    appcoinsRewards.getPayment(PACKAGE_NAME, SKU, PRICE.toString())
        .subscribe(statusObserver)

    scheduler.triggerActions()
    testObserver.assertNoErrors()
        .assertComplete()
    val mutableListOf = mutableListOf(
        Transaction(
            SKU, TYPE, STORE_ADDRESS, DEVELOPER_ADDRESS, OEM_ADDRESS, PACKAGE_NAME,
            PRICE, BDS_ORIGIN, Transaction.Status.PROCESSING, null, null, null, null, null, null,
            null
        ),
        Transaction(
            SKU, TYPE, STORE_ADDRESS, DEVELOPER_ADDRESS, OEM_ADDRESS, PACKAGE_NAME,
            PRICE, BDS_ORIGIN, Transaction.Status.COMPLETED, "0x32453134", null, null, null, null,
            null,
            null
        ))
    statusObserver.assertNoErrors()
        .assertValueSequence(mutableListOf)
  }

  @Test
  fun makePaymentUnityOrigin() {
    val testObserver = TestObserver<Any>()
    val origin = UNITY_ORIGIN
    appcoinsRewards.pay(
        PRICE, origin, SKU, TYPE, DEVELOPER_ADDRESS, STORE_ADDRESS, OEM_ADDRESS,
        PACKAGE_NAME, null, null, null, null, null
    )
        .subscribe(testObserver)
    val statusObserver = TestObserver<Transaction>()
    appcoinsRewards.getPayment(PACKAGE_NAME, SKU, PRICE.toString())
        .subscribe(statusObserver)

    scheduler.triggerActions()
    testObserver.assertNoErrors()
        .assertComplete()
    val mutableListOf = mutableListOf(
      Transaction(
        SKU, TYPE, DEVELOPER_ADDRESS, STORE_ADDRESS, OEM_ADDRESS, PACKAGE_NAME,
        PRICE, origin, Transaction.Status.PROCESSING, null, null, null, null, null, null, null
      ),
      Transaction(
        SKU, TYPE, DEVELOPER_ADDRESS, STORE_ADDRESS, OEM_ADDRESS, PACKAGE_NAME,
        PRICE, origin, Transaction.Status.COMPLETED, "0x32453134", null, null, null, null, null,
        null
      )
    )
    statusObserver.assertNoErrors()
        .assertValueSequence(mutableListOf)
  }

  @Test
  fun makePaymentUnknownOrigin() {
    val testObserver = TestObserver<Any>()
    val origin = UNKNOWN_ORIGIN
    appcoinsRewards.pay(
        PRICE, origin, SKU, TYPE, DEVELOPER_ADDRESS, STORE_ADDRESS, OEM_ADDRESS,
        PACKAGE_NAME, null, null, null, null, null
    )
        .subscribe(testObserver)
    val statusObserver = TestObserver<Transaction>()
    appcoinsRewards.getPayment(PACKAGE_NAME, SKU, PRICE.toString())
        .subscribe(statusObserver)

    scheduler.triggerActions()
    testObserver.assertNoErrors()
        .assertComplete()
    val mutableListOf = mutableListOf(
      Transaction(
        SKU, TYPE, DEVELOPER_ADDRESS, STORE_ADDRESS, OEM_ADDRESS, PACKAGE_NAME,
        PRICE, origin, Transaction.Status.PROCESSING, null, null, null, null, null, null, null
      ),
      Transaction(
        SKU, TYPE, DEVELOPER_ADDRESS, STORE_ADDRESS, OEM_ADDRESS, PACKAGE_NAME,
        PRICE, origin, Transaction.Status.COMPLETED, "0x32453134", null, null, null, null, null,
        null
      )
    )
    statusObserver.assertNoErrors()
        .assertValueSequence(mutableListOf)
  }

  @Test
  fun transferCredits() {
    val test = appcoinsRewards.sendCredits(DEVELOPER_ADDRESS, PRICE, PACKAGE_NAME)
        .test()
    scheduler.triggerActions()
    test.assertNoErrors()
        .assertComplete()
        .assertValue(AppcoinsRewardsRepository.Status.SUCCESS)
  }

  @Test
  fun transferCreditsNetworkError() {
    `when`(remoteApi.sendCredits(DEVELOPER_ADDRESS, USER_ADDRESS, USER_ADDRESS_SIGNATURE, PRICE,
        BDS_ORIGIN, TYPE_TRANSFER, PACKAGE_NAME)).thenReturn(
        Completable.error(HttpException(
            Response.error<AppcoinsRewardsRepository.Status>(400, ResponseBody.create(null, "")))))
    val test = appcoinsRewards.sendCredits(DEVELOPER_ADDRESS, PRICE, PACKAGE_NAME)
        .test()
    scheduler.triggerActions()
    test.assertNoErrors()
        .assertComplete()
        .assertValue(AppcoinsRewardsRepository.Status.API_ERROR)
  }

  @Test
  fun transferCreditsUnknownError() {
    `when`(remoteApi.sendCredits(DEVELOPER_ADDRESS, USER_ADDRESS, USER_ADDRESS_SIGNATURE, PRICE,
        BDS_ORIGIN, TYPE_TRANSFER, PACKAGE_NAME)).thenReturn(
        Completable.error(NullPointerException()))
    val test = appcoinsRewards.sendCredits(DEVELOPER_ADDRESS, PRICE, PACKAGE_NAME)
        .test()
    scheduler.triggerActions()
    test.assertNoErrors()
        .assertComplete()
        .assertValue(AppcoinsRewardsRepository.Status.UNKNOWN_ERROR)
  }
}
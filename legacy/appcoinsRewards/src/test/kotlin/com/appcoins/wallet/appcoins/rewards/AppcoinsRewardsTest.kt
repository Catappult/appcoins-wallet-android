package com.appcoins.wallet.appcoins.rewards

import com.appcoins.wallet.appcoins.rewards.repository.BdsAppcoinsRewardsRepository
import com.appcoins.wallet.appcoins.rewards.repository.RemoteRepository
import com.appcoins.wallet.appcoins.rewards.repository.WalletService
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.core.network.microservices.model.Gateway
import com.appcoins.wallet.core.utils.jvm_common.MemoryCache
import com.google.gson.Gson
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
import com.appcoins.wallet.core.network.microservices.model.Transaction as TransactionCore

@RunWith(MockitoJUnitRunner::class)
class AppcoinsRewardsTest {
  companion object {
    private const val USER_ADDRESS: String = "0xd9BA3c6932a5084D0CA0769893353D60b23AAfC4"
    private const val USER_ADDRESS_SIGNATURE: String =
      "27c3217155834a21fa8f97df99053f2874727837c03805c2eb1ba56383473b2a07fd865dd5db1359a717dfec9aa14bab6437184b14969ec3551b86e9d29c98d401"
    private const val OEM_ADDRESS: String = "0x652d25ac09f79e9619fba99f34f0d8420d0956b1"
    private const val STORE_ADDRESS: String = "0x652d25ac09f79e9619fba99f34f0d8420d0956b1"
    private const val SKU: String = "cm.aptoide.pt:gas"
    private const val TYPE: String = "INAPP"
    private const val TYPE_TRANSFER: String = "TRANSFER"
    private const val PACKAGE_NAME = "PACKAGE_NAME"
    private const val BDS_ORIGIN = "BDS"
    private const val UNITY_ORIGIN = "UNITY"
    private const val UNKNOWN_ORIGIN = "unknown"
    private val PRICE = BigDecimal("1700000000000000000")
    private const val EUR = "EUR"
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
    `when`(
      remoteApi.sendCredits(
        toWallet = "",
        walletAddress = USER_ADDRESS,
        amount = PRICE,
        currency = EUR,
        origin = BDS_ORIGIN,
        type = TYPE_TRANSFER,
        packageName = PACKAGE_NAME,
        guestWalletId = null
      )
    ).thenReturn(
      Single.just(
        com.appcoins.wallet.core.network.microservices.model.Transaction(
          uid = "123456789",
          status = com.appcoins.wallet.core.network.microservices.model.Transaction.Status.PROCESSING,
          gateway = null,
          hash = null,
          metadata = null,
          orderReference = null,
          price = null,
          type = "",
          wallets = null,
          url = null
        )
      )
    )

    `when`(
      remoteApi.pay(
        walletAddress = USER_ADDRESS,
        signature = USER_ADDRESS_SIGNATURE,
        amount = PRICE,
        origin = BDS_ORIGIN,
        sku = SKU,
        type = TYPE,
        entityOemId = STORE_ADDRESS,
        entityDomain = OEM_ADDRESS,
        packageName = PACKAGE_NAME,
        payload = null,
        callback = null,
        orderReference = null,
        referrerUrl = null,
        guestWalletId = null
      )
    ).thenReturn(
      Single.just(
        TransactionCore(
          uid = UID,
          status = TransactionCore.Status.COMPLETED,
          gateway = Gateway.unknown(),
          hash = "0x32453134",
          metadata = null,
          orderReference = "orderReference",
          price = null,
          type = "",
          wallets = null
        )
      )
    )

    `when`(
      remoteApi.pay(
        walletAddress = USER_ADDRESS,
        signature = USER_ADDRESS_SIGNATURE,
        amount = PRICE,
        origin = UNITY_ORIGIN,
        sku = SKU,
        type = TYPE,
        entityOemId = STORE_ADDRESS,
        entityDomain = OEM_ADDRESS,
        packageName = PACKAGE_NAME,
        payload = null,
        callback = null,
        orderReference = null,
        referrerUrl = null,
        guestWalletId = null
      )
    ).thenReturn(
      Single.just(
        TransactionCore(
          uid = UID,
          status = TransactionCore.Status.COMPLETED,
          gateway = Gateway.unknown(),
          hash = "0x32453134",
          metadata = null,
          orderReference = "orderReference",
          price = null,
          type = "",
          wallets = null
        )
      )
    )

    `when`(
      remoteApi.pay(
        walletAddress = USER_ADDRESS,
        signature = USER_ADDRESS_SIGNATURE,
        amount = PRICE,
        origin = null,
        sku = SKU,
        type = TYPE,
        entityOemId = STORE_ADDRESS,
        entityDomain = OEM_ADDRESS,
        packageName = PACKAGE_NAME,
        payload = null,
        callback = null,
        orderReference = null,
        referrerUrl = null,
        guestWalletId = null
      )
    ).thenReturn(
      Single.just(
        TransactionCore(
          uid = UID,
          status = TransactionCore.Status.COMPLETED,
          gateway = Gateway.unknown(),
          hash = "0x32453134",
          metadata = null,
          orderReference = "orderReference",
          price = null,
          type = "",
          wallets = null
        )
      )
    )

    `when`(billing.getAppcoinsTransaction(UID, scheduler)).thenReturn(
      Single.just(
        TransactionCore(
          uid = UID,
          status = TransactionCore.Status.COMPLETED,
          gateway = Gateway.unknown(),
          hash = "0x32453134",
          metadata = null,
          orderReference = "orderReference",
          price = null,
          type = "",
          wallets = null
        )
      )
    )

    scheduler.advanceTimeBy(1, TimeUnit.DAYS)
    scheduler.triggerActions()

    appcoinsRewards =
      AppcoinsRewards(
        repository = BdsAppcoinsRewardsRepository(remoteApi),
        walletService = object : WalletService {
          override fun getWalletAddress(): Single<String> {
            return Single.just(USER_ADDRESS)
          }

          override fun signContent(content: String): Single<String> {
            return Single.just(USER_ADDRESS_SIGNATURE)

          }
        },
        cache = MemoryCache(
          BehaviorSubject.create(),
          ConcurrentHashMap()
        ),
        scheduler = scheduler,
        billing = billing,
        errorMapper = ErrorMapper(Gson())
      )
    appcoinsRewards.start()
  }

  @Test
  fun makePayment() {
    val testObserver = TestObserver<Any>()
    appcoinsRewards.pay(
      amount = PRICE,
      origin = BDS_ORIGIN,
      sku = SKU,
      type = TYPE,
      entityOemId = STORE_ADDRESS,
      entityDomainId = OEM_ADDRESS,
      packageName = PACKAGE_NAME,
      payload = null,
      callbackUrl = null,
      orderReference = null,
      referrerUrl = null,
      productToken = null,
      guestWalletId = null
    )
      .subscribe(testObserver)
    val statusObserver = TestObserver<Transaction>()
    appcoinsRewards.getPayment(
      packageName = PACKAGE_NAME,
      sku = SKU,
      amount = PRICE.toString()
    )
      .subscribe(statusObserver)

    scheduler.triggerActions()
    testObserver.assertNoErrors()
      .assertComplete()
    val mutableListOf = mutableListOf(
      Transaction(
        sku = SKU,
        type = TYPE,
        developerAddress = "",
        entityOemId = STORE_ADDRESS,
        entityDomain = OEM_ADDRESS,
        packageName = PACKAGE_NAME,
        amount = PRICE,
        origin = BDS_ORIGIN,
        status = Transaction.Status.PROCESSING,
        txId = null,
        purchaseUid = null,
        payload = null,
        callback = null,
        orderReference = null,
        referrerUrl = null,
        productToken = null,
        guestWalletId = null
      ),
      Transaction(
        sku = SKU,
        type = TYPE,
        developerAddress = "",
        entityOemId = STORE_ADDRESS,
        entityDomain = OEM_ADDRESS,
        packageName = PACKAGE_NAME,
        amount = PRICE,
        origin = BDS_ORIGIN,
        status = Transaction.Status.COMPLETED,
        txId = UID,
        purchaseUid = null,
        payload = null,
        callback = null,
        orderReference = null,
        referrerUrl = null,
        productToken = null,
        guestWalletId = null
      )
    )
    statusObserver.assertNoErrors()
      .assertValueSequence(mutableListOf)
  }

  @Test
  fun makePaymentUnityOrigin() {
    val testObserver = TestObserver<Any>()
    val origin = UNITY_ORIGIN
    appcoinsRewards.pay(
      amount = PRICE,
      origin = origin,
      sku = SKU,
      type = TYPE,
      entityOemId = STORE_ADDRESS,
      entityDomainId = OEM_ADDRESS,
      packageName = PACKAGE_NAME,
      payload = null,
      callbackUrl = null,
      orderReference = null,
      referrerUrl = null,
      productToken = null,
      guestWalletId = null
    )
      .subscribe(testObserver)
    val statusObserver = TestObserver<Transaction>()
    appcoinsRewards.getPayment(
      packageName = PACKAGE_NAME,
      sku = SKU,
      amount = PRICE.toString()
    )
      .subscribe(statusObserver)

    scheduler.triggerActions()
    testObserver.assertNoErrors()
      .assertComplete()
    val mutableListOf = mutableListOf(
      Transaction(
        sku = SKU,
        type = TYPE,
        developerAddress = "",
        entityOemId = STORE_ADDRESS,
        entityDomain = OEM_ADDRESS,
        packageName = PACKAGE_NAME,
        amount = PRICE,
        origin = origin,
        status = Transaction.Status.PROCESSING,
        txId = null,
        purchaseUid = null,
        payload = null,
        callback = null,
        orderReference = null,
        referrerUrl = null,
        productToken = null,
        guestWalletId = null
      ),
      Transaction(
        sku = SKU,
        type = TYPE,
        developerAddress = "",
        entityOemId = STORE_ADDRESS,
        entityDomain = OEM_ADDRESS,
        packageName = PACKAGE_NAME,
        amount = PRICE,
        origin = origin,
        status = Transaction.Status.COMPLETED,
        txId = UID,
        purchaseUid = null,
        payload = null,
        callback = null,
        orderReference = null,
        referrerUrl = null,
        productToken = null,
        guestWalletId = null
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
      amount = PRICE,
      origin = origin,
      sku = SKU,
      type = TYPE,
      entityOemId = STORE_ADDRESS,
      entityDomainId = OEM_ADDRESS,
      packageName = PACKAGE_NAME,
      payload = null,
      callbackUrl = null,
      orderReference = null,
      referrerUrl = null,
      productToken = null,
      guestWalletId = null
    )
      .subscribe(testObserver)
    val statusObserver = TestObserver<Transaction>()
    appcoinsRewards.getPayment(
      packageName = PACKAGE_NAME,
      sku = SKU,
      amount = PRICE.toString()
    )
      .subscribe(statusObserver)

    scheduler.triggerActions()
    testObserver.assertNoErrors()
      .assertComplete()
    val mutableListOf = mutableListOf(
      Transaction(
        sku = SKU,
        type = TYPE,
        developerAddress = "",
        entityOemId = STORE_ADDRESS,
        entityDomain = OEM_ADDRESS,
        packageName = PACKAGE_NAME,
        amount = PRICE,
        origin = origin,
        status = Transaction.Status.PROCESSING,
        txId = null,
        purchaseUid = null,
        payload = null,
        callback = null,
        orderReference = null,
        referrerUrl = null,
        productToken = null,
        guestWalletId = null
      ),
      Transaction(
        sku = SKU,
        type = TYPE,
        developerAddress = "",
        entityOemId = STORE_ADDRESS,
        entityDomain = OEM_ADDRESS,
        packageName = PACKAGE_NAME,
        amount = PRICE,
        origin = origin,
        status = Transaction.Status.COMPLETED,
        txId = UID,
        purchaseUid = null,
        payload = null,
        callback = null,
        orderReference = null,
        referrerUrl = null,
        productToken = null,
        guestWalletId = null
      )
    )
    statusObserver.assertNoErrors()
      .assertValueSequence(mutableListOf)
  }

  @Test
  fun transferCredits() {
    val test = appcoinsRewards.sendCredits(
      toWallet = "",
      amount = PRICE,
      currency = EUR,
      packageName = PACKAGE_NAME,
      guestWalletId = null
    )
      .test()
    scheduler.triggerActions()
    test.assertNoErrors()
      .assertComplete()
      .assertValue(AppcoinsRewardsRepository.Status.SUCCESS)
  }

  @Test
  fun transferCreditsNetworkError() {
    `when`(
      remoteApi.sendCredits(
        toWallet = "",
        walletAddress = USER_ADDRESS,
        amount = PRICE,
        currency = EUR,
        origin = BDS_ORIGIN,
        type = TYPE_TRANSFER,
        packageName = PACKAGE_NAME,
        guestWalletId = null
      )
    ).thenReturn(
      Single.error(
        HttpException(
          Response.error<AppcoinsRewardsRepository.Status>(400, ResponseBody.create(null, ""))
        )
      )
    )
    val test = appcoinsRewards.sendCredits(
      toWallet = "",
      amount = PRICE,
      currency = EUR,
      packageName = PACKAGE_NAME,
      guestWalletId = null
    )
      .test()
    scheduler.triggerActions()
    test.assertNoErrors()
      .assertComplete()
      .assertValue(AppcoinsRewardsRepository.Status.API_ERROR)
  }

  @Test
  fun transferCreditsUnknownError() {
    `when`(
      remoteApi.sendCredits(
        toWallet = "",
        walletAddress = USER_ADDRESS,
        amount = PRICE,
        currency = EUR,
        origin = BDS_ORIGIN,
        type = TYPE_TRANSFER,
        packageName = PACKAGE_NAME,
        guestWalletId = null
      )
    ).thenReturn(
      Single.error(NullPointerException())
    )
    val test = appcoinsRewards.sendCredits(
      toWallet = "",
      amount = PRICE,
      currency = EUR,
      packageName = PACKAGE_NAME,
      guestWalletId = null
    )
      .test()
    scheduler.triggerActions()
    test.assertNoErrors()
      .assertComplete()
      .assertValue(AppcoinsRewardsRepository.Status.UNKNOWN_ERROR)
  }
}
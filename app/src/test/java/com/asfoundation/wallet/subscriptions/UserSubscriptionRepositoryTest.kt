package com.asfoundation.wallet.subscriptions

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.subscriptions.*
import com.appcoins.wallet.core.network.microservices.model.*
import com.asfoundation.wallet.util.Period
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class UserSubscriptionRepositoryTest {

  private companion object {
    private const val TEST_WALLET_ADDRESS = "0x123"
    private const val TEST_UID = "uid"
    private const val TEST_SKU = "sku"
    private const val TEST_TITLE = "title"
    private const val TEST_PERIOD = "period"
    private const val TEST_STARTED = "started"
    private const val TEST_RENEWAL = "renewal"
    private const val TEST_EXPIRE = "expire"
    private const val TEST_ENDED = "ended"
    private const val TEST_NAME = "name"
    private const val TEST_ICON = "icon"
    private const val TEST_GATEWAY = "gateway"
    private const val TEST_REFERENCE = "reference"
    private const val TEST_LABEL = "1.00 USD"
    private const val TEST_APPC_LABEL = "1.00 APPC"
    private const val TEST_CURRENCY = "USD"
    private const val TEST_SYMBOL = "$"
    private const val TEST_CREATED = "created"
    private const val TEST_PACKAGE_NAME = "trivial"
    private const val TEST_PAYMENT_METHOD = "credit_card"
    private const val TEST_PAYMENT_TITLE = "Credit Card"
    private const val TEST_PAYMENT_ICON = "icon"
    private const val TEST_EXPIRED_LIMIT = 6
    private val TEST_FIAT_AMOUNT = BigDecimal.ONE
    private val TEST_APPC_AMOUNT = BigDecimal.TEN
  }

  @Mock
  lateinit var api: UserSubscriptionApi

  @Mock
  lateinit var localData: UserSubscriptionsLocalData

  @Mock
  lateinit var walletService: WalletService

  @Mock
  lateinit var mapper: UserSubscriptionsMapper

  private lateinit var activeItem: SubscriptionItem
  private lateinit var expiredItem: SubscriptionItem
  private lateinit var pendingItem: SubscriptionItem
  private lateinit var listResponse: UserSubscriptionsListResponse
  private lateinit var expiredListResponse: UserSubscriptionsListResponse
  private lateinit var activeSubResponse: UserSubscriptionResponse
  private lateinit var expiredSubResponse: UserSubscriptionResponse
  private lateinit var pendingSubResponse: UserSubscriptionResponse
  private lateinit var userSubscriptionRepository: UserSubscriptionRepository

  @Before
  fun setup() {
    val applicationResponse = ApplicationInfoResponse(TEST_PACKAGE_NAME, TEST_TITLE, TEST_ICON)
    val orderResponse =
        OrderResponse(TEST_GATEWAY, TEST_REFERENCE, TEST_FIAT_AMOUNT, TEST_LABEL, TEST_CURRENCY,
            TEST_SYMBOL, TEST_CREATED,
            MethodResponse(TEST_PAYMENT_METHOD, TEST_PAYMENT_TITLE, TEST_PAYMENT_ICON),
            AppcPrice(TEST_APPC_AMOUNT, TEST_APPC_LABEL))
    activeSubResponse =
        UserSubscriptionResponse(TEST_UID, TEST_SKU, TEST_TITLE, TEST_PERIOD,
            SubscriptionSubStatus.ACTIVE, TEST_STARTED, TEST_RENEWAL, TEST_EXPIRE, null,
            applicationResponse, orderResponse)
    expiredSubResponse =
        UserSubscriptionResponse(TEST_UID, TEST_SKU, TEST_TITLE, TEST_PERIOD,
            SubscriptionSubStatus.EXPIRED, TEST_STARTED, TEST_RENEWAL, null, TEST_ENDED,
            applicationResponse, orderResponse)
    pendingSubResponse =
        UserSubscriptionResponse(TEST_UID, TEST_SKU, TEST_TITLE, TEST_PERIOD,
            SubscriptionSubStatus.PENDING,
            null, null, null, null, applicationResponse, orderResponse)
    Mockito.`when`(walletService.signContent(TEST_WALLET_ADDRESS))
        .thenReturn(Single.just(TEST_WALLET_ADDRESS))

    val userSubList = listOf(activeSubResponse, expiredSubResponse, pendingSubResponse)
    listResponse = UserSubscriptionsListResponse(userSubList)
    expiredListResponse = UserSubscriptionsListResponse(listOf(expiredSubResponse))
    activeItem =
        SubscriptionItem("sku", TEST_NAME, Period(0, 0, 1, 0), Status.ACTIVE, null, null, null,
            null,
            TEST_PACKAGE_NAME, TEST_NAME, TEST_ICON, TEST_FIAT_AMOUNT, TEST_SYMBOL, TEST_CURRENCY,
            TEST_PAYMENT_METHOD, TEST_PAYMENT_ICON, TEST_APPC_AMOUNT, TEST_APPC_LABEL, TEST_UID)
    expiredItem =
        SubscriptionItem("sku", TEST_NAME, Period(0, 0, 1, 0), Status.EXPIRED, null, null, null,
            null,
            TEST_PACKAGE_NAME, TEST_NAME, TEST_ICON, TEST_FIAT_AMOUNT, TEST_SYMBOL, TEST_CURRENCY,
            TEST_PAYMENT_METHOD, TEST_PAYMENT_ICON, TEST_APPC_AMOUNT, TEST_APPC_LABEL, TEST_UID)
    pendingItem =
        SubscriptionItem("sku", TEST_NAME, Period(0, 0, 1, 0), Status.PENDING, null, null, null,
            null,
            TEST_PACKAGE_NAME, TEST_NAME, TEST_ICON, TEST_FIAT_AMOUNT, TEST_SYMBOL, TEST_CURRENCY,
            TEST_PAYMENT_METHOD, TEST_PAYMENT_ICON, TEST_APPC_AMOUNT, TEST_APPC_LABEL, TEST_UID)
    userSubscriptionRepository = UserSubscriptionRepository(api, localData, walletService, mapper)
  }

  @Test
  fun getUserSubscriptionsFromApiOnlyTest() {
    val subscriptionModel =
        SubscriptionModel(listOf(activeItem, expiredItem, pendingItem), listOf(expiredItem), false)
    val observer = TestObserver<SubscriptionModel>()
    handleAPIMocks(subscriptionModel)

    userSubscriptionRepository.getUserSubscriptions(TEST_WALLET_ADDRESS, true)
        .subscribe(observer)

    observer.assertNoErrors()
        .assertValue { it == subscriptionModel }
    Mockito.verify(localData)
        .insertSubscriptions(listResponse.items, TEST_WALLET_ADDRESS)
  }

  @Test
  fun getUserSubscriptionsDbAndApiTest() {
    val dbSubscriptionModel =
        SubscriptionModel(listOf(activeItem, expiredItem, pendingItem), listOf(expiredItem), true)
    val apiSubscriptionModel =
        SubscriptionModel(listOf(activeItem, expiredItem, pendingItem), listOf(expiredItem), false)
    val observer = TestObserver<SubscriptionModel>()

    handleDbMocks(dbSubscriptionModel)
    handleAPIMocks(apiSubscriptionModel)

    userSubscriptionRepository.getUserSubscriptions(TEST_WALLET_ADDRESS, false)
        .subscribe(observer)

    observer.assertNoErrors()
        .assertValueCount(2)
        .assertValues(dbSubscriptionModel, apiSubscriptionModel)
    Mockito.verify(localData)
        .insertSubscriptions(listResponse.items, TEST_WALLET_ADDRESS)
  }

  @Test
  fun getUserSubscriptionsDbErrorTest() {
    val dbSubscriptionModel = SubscriptionModel(true, SubscriptionModel.Error.UNKNOWN)
    handleDbErrorMocks(dbSubscriptionModel)
    val apiSubscriptionModel =
        SubscriptionModel(listOf(activeItem, expiredItem, pendingItem), listOf(expiredItem), false)
    val observer = TestObserver<SubscriptionModel>()

    handleAPIMocks(apiSubscriptionModel)

    userSubscriptionRepository.getUserSubscriptions(TEST_WALLET_ADDRESS, false)
        .subscribe(observer)

    observer.assertValues(dbSubscriptionModel, apiSubscriptionModel)
  }

  @Test
  fun getUserSubscriptionsApiErrorTest() {
    val dbSubscriptionModel =
        SubscriptionModel(listOf(activeItem, expiredItem, pendingItem), listOf(expiredItem), true)
    handleDbMocks(dbSubscriptionModel)
    val apiSubscriptionModel = SubscriptionModel(false, SubscriptionModel.Error.UNKNOWN)
    val observer = TestObserver<SubscriptionModel>()

    handleErrorAPIMocks(apiSubscriptionModel)

    userSubscriptionRepository.getUserSubscriptions(TEST_WALLET_ADDRESS, false)
        .subscribe(observer)

    observer.assertValues(dbSubscriptionModel, apiSubscriptionModel)
  }

  private fun handleDbErrorMocks(dbSubscriptionModel: SubscriptionModel) {
    val throwable = Throwable("an error")
    Mockito.`when`(
        localData.getSubscriptions(TEST_WALLET_ADDRESS))
        .thenReturn(Observable.error(throwable))
    Mockito.`when`(
        localData.getSubscriptions(TEST_WALLET_ADDRESS, SubscriptionSubStatus.EXPIRED,
            TEST_EXPIRED_LIMIT))
        .thenReturn(Observable.error(throwable))
    Mockito.`when`(mapper.mapError(throwable, true))
        .thenReturn(dbSubscriptionModel)
  }

  private fun handleErrorAPIMocks(subscriptionModel: SubscriptionModel) {
    val throwable = Throwable("an error")
    val locale = Locale.getDefault()
        .toLanguageTag()
    Mockito.`when`(
        api.getUserSubscriptions(locale, TEST_WALLET_ADDRESS, TEST_WALLET_ADDRESS, null, null,
            null))
        .thenReturn(Single.error(throwable))
    Mockito.`when`(
        api.getUserSubscriptions(locale, TEST_WALLET_ADDRESS, TEST_WALLET_ADDRESS, "EXPIRED",
            TEST_EXPIRED_LIMIT, null))
        .thenReturn(Single.error(throwable))
    Mockito.`when`(mapper.mapError(throwable, false))
        .thenReturn(subscriptionModel)
  }

  private fun handleDbMocks(subscriptionModel: SubscriptionModel) {
    Mockito.`when`(
        localData.getSubscriptions(TEST_WALLET_ADDRESS))
        .thenReturn(Observable.just(listResponse))
    Mockito.`when`(
        localData.getSubscriptions(TEST_WALLET_ADDRESS, SubscriptionSubStatus.EXPIRED,
            TEST_EXPIRED_LIMIT))
        .thenReturn(Observable.just(expiredListResponse))
    Mockito.`when`(mapper.mapToSubscriptionModel(listResponse, expiredListResponse, true))
        .thenReturn(subscriptionModel)
  }

  private fun handleAPIMocks(subscriptionModel: SubscriptionModel) {
    val locale = Locale.getDefault()
        .toLanguageTag()
    Mockito.`when`(
        api.getUserSubscriptions(locale, TEST_WALLET_ADDRESS, TEST_WALLET_ADDRESS, null, null,
            null))
        .thenReturn(Single.just(listResponse))
    Mockito.`when`(
        api.getUserSubscriptions(locale, TEST_WALLET_ADDRESS, TEST_WALLET_ADDRESS, "EXPIRED",
            TEST_EXPIRED_LIMIT, null))
        .thenReturn(Single.just(expiredListResponse))
    Mockito.`when`(mapper.mapToSubscriptionModel(listResponse, expiredListResponse, false))
        .thenReturn(subscriptionModel)
  }
}
package com.asfoundation.wallet.subscriptions

import com.appcoins.wallet.bdsbilling.subscriptions.*
import com.appcoins.wallet.core.network.microservices.model.*
import com.asfoundation.wallet.util.Period
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException
import java.math.BigDecimal
import java.text.SimpleDateFormat

@RunWith(MockitoJUnitRunner::class)
class UserSubscriptionsMapperTest {

  private companion object {
    private const val TEST_UID = "uid"
    private const val TEST_SKU = "sku"
    private const val TEST_TITLE = "title"
    private const val TEST_PERIOD = "P1W"
    private const val TEST_STARTED = "2020-04-01T10:27:45.823910Z"
    private const val TEST_RENEWAL = "2020-04-01T10:27:45.823910Z"
    private const val TEST_EXPIRE = "2020-04-01T10:27:45.823910Z"
    private const val TEST_ENDED = "2020-04-01T10:27:45.823910Z"
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
    private val TEST_FIAT_AMOUNT = BigDecimal.ONE
    private val TEST_APPC_AMOUNT = BigDecimal.TEN
  }

  private lateinit var mapper: UserSubscriptionsMapper

  @Before
  fun setup() {
    mapper = UserSubscriptionsMapper()
  }

  @Test
  fun mapResponseTest() {
    val dateFormat = SimpleDateFormat(UserSubscriptionsMapper.DATE_FORMAT_PATTERN,
        UserSubscriptionsMapper.LOCALE)
    val expectedDate = dateFormat.parse(TEST_STARTED)
    val applicationResponse = ApplicationInfoResponse(TEST_PACKAGE_NAME, TEST_TITLE, TEST_ICON)
    val orderResponse = OrderResponse(TEST_GATEWAY, TEST_REFERENCE, TEST_FIAT_AMOUNT, TEST_LABEL,
        TEST_CURRENCY, TEST_SYMBOL, TEST_CREATED,
        MethodResponse(TEST_PAYMENT_METHOD, TEST_PAYMENT_TITLE, TEST_PAYMENT_ICON),
        AppcPrice(TEST_APPC_AMOUNT, TEST_APPC_LABEL))
    val activeSubResponse = UserSubscriptionResponse(TEST_UID, TEST_SKU, TEST_TITLE, TEST_PERIOD,
        SubscriptionSubStatus.ACTIVE, TEST_STARTED, TEST_RENEWAL, TEST_EXPIRE, null,
        applicationResponse, orderResponse)
    val expiredSubResponse = UserSubscriptionResponse(TEST_UID, TEST_SKU, TEST_TITLE, TEST_PERIOD,
        SubscriptionSubStatus.EXPIRED, TEST_STARTED, TEST_RENEWAL, null, TEST_ENDED,
        applicationResponse, orderResponse)
    val responseCanceled = UserSubscriptionResponse(TEST_UID, TEST_SKU, TEST_TITLE, TEST_PERIOD,
        SubscriptionSubStatus.CANCELED, TEST_STARTED, TEST_RENEWAL, TEST_EXPIRE, null,
        applicationResponse, orderResponse)
    val responsePaused = UserSubscriptionResponse(TEST_UID, TEST_SKU, TEST_TITLE, TEST_PERIOD,
        SubscriptionSubStatus.PAUSED, TEST_STARTED, null, null, null, applicationResponse,
        orderResponse)
    val responseRevoked = UserSubscriptionResponse(TEST_UID, TEST_SKU, TEST_TITLE, TEST_PERIOD,
        SubscriptionSubStatus.REVOKED, TEST_STARTED, null, TEST_EXPIRE, TEST_ENDED,
        applicationResponse, orderResponse)
    val responsePending = UserSubscriptionResponse(TEST_UID, TEST_SKU, TEST_TITLE, TEST_PERIOD,
        SubscriptionSubStatus.PENDING, null, null, null, null, applicationResponse, orderResponse)
    val responseGrace = UserSubscriptionResponse(TEST_UID, TEST_SKU, TEST_TITLE, TEST_PERIOD,
        SubscriptionSubStatus.GRACE, TEST_STARTED, TEST_RENEWAL, TEST_EXPIRE, null,
        applicationResponse, orderResponse)
    val responseHold = UserSubscriptionResponse(TEST_UID, TEST_SKU, TEST_TITLE, TEST_PERIOD,
        SubscriptionSubStatus.HOLD, TEST_STARTED, TEST_RENEWAL, TEST_EXPIRE, null,
        applicationResponse, orderResponse)
    val allSubscriptions = UserSubscriptionsListResponse(
        listOf(activeSubResponse, expiredSubResponse, responseCanceled, responsePaused,
            responseRevoked, responsePending, responseGrace, responseHold))
    val expiredSubscriptions = UserSubscriptionsListResponse(listOf(expiredSubResponse))
    val model = mapper.mapToSubscriptionModel(allSubscriptions, expiredSubscriptions, true)
    val activeItem =
        SubscriptionItem("sku", TEST_TITLE, Period(0, 0, 1, 0), Status.ACTIVE, expectedDate,
            expectedDate,
            expectedDate, null, TEST_PACKAGE_NAME, TEST_TITLE, TEST_ICON, TEST_FIAT_AMOUNT,
            TEST_SYMBOL, TEST_CURRENCY, TEST_PAYMENT_TITLE, TEST_PAYMENT_ICON, TEST_APPC_AMOUNT,
            TEST_APPC_LABEL, TEST_UID)
    val expiredItem =
        SubscriptionItem("sku", TEST_TITLE, Period(0, 0, 1, 0), Status.EXPIRED, expectedDate,
            expectedDate,
            null, expectedDate, TEST_PACKAGE_NAME, TEST_TITLE, TEST_ICON, TEST_FIAT_AMOUNT,
            TEST_SYMBOL, TEST_CURRENCY, TEST_PAYMENT_TITLE, TEST_PAYMENT_ICON, TEST_APPC_AMOUNT,
            TEST_APPC_LABEL, TEST_UID)
    val canceledItem =
        SubscriptionItem("sku", TEST_TITLE, Period(0, 0, 1, 0), Status.CANCELED, expectedDate,
            expectedDate, expectedDate, null, TEST_PACKAGE_NAME, TEST_TITLE, TEST_ICON,
            TEST_FIAT_AMOUNT, TEST_SYMBOL, TEST_CURRENCY, TEST_PAYMENT_TITLE, TEST_PAYMENT_ICON,
            TEST_APPC_AMOUNT, TEST_APPC_LABEL, TEST_UID)
    val pausedItem =
        SubscriptionItem("sku", TEST_TITLE, Period(0, 0, 1, 0), Status.PAUSED, expectedDate, null,
            null,
            null, TEST_PACKAGE_NAME, TEST_TITLE, TEST_ICON, TEST_FIAT_AMOUNT, TEST_SYMBOL,
            TEST_CURRENCY, TEST_PAYMENT_TITLE, TEST_PAYMENT_ICON, TEST_APPC_AMOUNT, TEST_APPC_LABEL,
            TEST_UID)
    val revokedItem =
        SubscriptionItem("sku", TEST_TITLE, Period(0, 0, 1, 0), Status.REVOKED, expectedDate, null,
            expectedDate, expectedDate, TEST_PACKAGE_NAME, TEST_TITLE, TEST_ICON, TEST_FIAT_AMOUNT,
            TEST_SYMBOL, TEST_CURRENCY, TEST_PAYMENT_TITLE, TEST_PAYMENT_ICON, TEST_APPC_AMOUNT,
            TEST_APPC_LABEL, TEST_UID)
    val pendingItem =
        SubscriptionItem("sku", TEST_TITLE, Period(0, 0, 1, 0), Status.PENDING, null, null, null,
            null,
            TEST_PACKAGE_NAME, TEST_TITLE, TEST_ICON, TEST_FIAT_AMOUNT, TEST_SYMBOL, TEST_CURRENCY,
            TEST_PAYMENT_TITLE, TEST_PAYMENT_ICON, TEST_APPC_AMOUNT, TEST_APPC_LABEL, TEST_UID)
    val gracePeriodItem =
        SubscriptionItem("sku", TEST_TITLE, Period(0, 0, 1, 0), Status.GRACE, expectedDate,
            expectedDate,
            expectedDate, null, TEST_PACKAGE_NAME, TEST_TITLE, TEST_ICON, TEST_FIAT_AMOUNT,
            TEST_SYMBOL, TEST_CURRENCY, TEST_PAYMENT_TITLE, TEST_PAYMENT_ICON, TEST_APPC_AMOUNT,
            TEST_APPC_LABEL, TEST_UID)
    val onHoldItem =
        SubscriptionItem("sku", TEST_TITLE, Period(0, 0, 1, 0), Status.HOLD, expectedDate,
            expectedDate,
            expectedDate, null, TEST_PACKAGE_NAME, TEST_TITLE, TEST_ICON, TEST_FIAT_AMOUNT,
            TEST_SYMBOL, TEST_CURRENCY, TEST_PAYMENT_TITLE, TEST_PAYMENT_ICON, TEST_APPC_AMOUNT,
            TEST_APPC_LABEL, TEST_UID)
    val allItemsList = listOf(activeItem, expiredItem, canceledItem, pausedItem, revokedItem,
        pendingItem, gracePeriodItem, onHoldItem)
    val expiredItemList = listOf(expiredItem)
    val expectedModel = SubscriptionModel(allItemsList, expiredItemList, true)
    Assert.assertEquals(expectedModel.toString(), model.toString())
  }

  @Test
  fun mapErrorTest() {
    val t = Throwable("an error")
    val model = mapper.mapError(t, true)
    val expectedModel = SubscriptionModel(true, SubscriptionModel.Error.UNKNOWN)
    Assert.assertEquals(model, expectedModel)
  }

  @Test
  fun mapNetworkErrorTest() {
    val t = Throwable(IOException())
    val model = mapper.mapError(t, true)
    val expectedModel = SubscriptionModel(true, SubscriptionModel.Error.NO_NETWORK)
    Assert.assertEquals(model, expectedModel)
  }
}
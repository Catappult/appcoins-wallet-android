package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.appcoins.wallet.billing.util.Error
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.microservices.api.broker.AdyenApi
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.core.network.microservices.model.AdyenTransactionResponse
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerVerificationApi
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.network.microservices.api.product.SubscriptionBillingApi
import com.appcoins.wallet.core.network.microservices.model.*
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.core.walletservices.WalletServices.WalletAddressModel
import com.google.gson.JsonObject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal

@RunWith(MockitoJUnitRunner::class)
class AdyenPaymentRepositoryTest {

  @Mock
  lateinit var adyenApi: AdyenApi

  @Mock
  lateinit var brokerBdsApi: BrokerBdsApi

  @Mock
  lateinit var subscriptionsApi: SubscriptionBillingApi

  @Mock
  lateinit var mapper: AdyenResponseMapper

  @Mock
  lateinit var logger: Logger

//  @Mock
  lateinit var ewtAuthenticatorService: EwtAuthenticatorService

//  @Mock
  lateinit var rxSchedulers: RxSchedulers

  private lateinit var adyenRepo: AdyenPaymentRepository

  companion object {
    private const val TEST_WALLET_ADDRESS = "0x123"
    private const val TEST_WALLET_SIGNATURE = "0x987"
    private const val TEST_EWT = "123456789"
    private const val TEST_FIAT_VALUE = "2.0"
    private const val TEST_FIAT_CURRENCY = "EUR"
    private const val TEST_UID = "uid"
  }

  @Before
  fun setup() {
    ewtAuthenticatorService = EwtAuthenticatorService(
      object: WalletService {
        override fun getWalletAddress(): Single<String> = Single.just(TEST_WALLET_ADDRESS)
        override fun getWalletOrCreate(): Single<String> = Single.just(TEST_WALLET_ADDRESS)
        override fun findWalletOrCreate(): Observable<String> = Observable.just(TEST_WALLET_ADDRESS)
        override fun signContent(content: String): Single<String> = Single.just(TEST_WALLET_SIGNATURE)
        override fun signSpecificWalletAddressContent(
          walletAddress: String,
          content: String
        ): Single<String> = Single.just(TEST_WALLET_SIGNATURE)
        override fun getAndSignCurrentWalletAddress(): Single<WalletAddressModel> = Single.just(WalletAddressModel(TEST_WALLET_ADDRESS,TEST_WALLET_SIGNATURE))
        override fun getAndSignSpecificWalletAddress(walletAddress: String): Single<WalletAddressModel> = Single.just(WalletAddressModel(TEST_WALLET_ADDRESS,TEST_WALLET_SIGNATURE))
      }
    , "11223344" )

    rxSchedulers = object : RxSchedulers {
      override val main: Scheduler
        get() = TestScheduler()
      override val io: Scheduler
        get() = TestScheduler()
      override val computation: Scheduler
        get() = TestScheduler()
    }

    adyenRepo = AdyenPaymentRepository(adyenApi, brokerBdsApi, subscriptionsApi, mapper, ewtAuthenticatorService, rxSchedulers,logger)
  }

  @Test
  fun loadPaymentInfoTest() {
    val response = PaymentMethodsResponse(
      AdyenPrice(
        BigDecimal(2),
        TEST_FIAT_CURRENCY
      ), JsonObject()
    )

    val model = PaymentInfoModel(null, false, BigDecimal(2),
        TEST_FIAT_CURRENCY)
    Mockito.`when`(
        adyenApi.loadPaymentInfo(
            TEST_WALLET_ADDRESS,
            TEST_WALLET_SIGNATURE,
            TEST_EWT,
            TEST_FIAT_VALUE,
            TEST_FIAT_CURRENCY,
            AdyenPaymentRepository.Methods.CREDIT_CARD.transactionType))
        .thenReturn(Single.just(response))

    Mockito.`when`(mapper.map(response, AdyenPaymentRepository.Methods.CREDIT_CARD))
        .thenReturn(model)
    val testObserver = TestObserver<PaymentInfoModel>()

    adyenRepo.loadPaymentInfo(
        AdyenPaymentRepository.Methods.CREDIT_CARD,
        TEST_FIAT_VALUE,
        TEST_FIAT_CURRENCY,
        TEST_WALLET_ADDRESS,
        TEST_WALLET_SIGNATURE,
        TEST_EWT
    )
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == model }
  }

  @Test
  fun loadPaymentInfoErrorTest() {
    val throwable = Throwable("Error")
    val model = PaymentInfoModel(Error())
    Mockito.`when`(
        adyenApi.loadPaymentInfo(
            TEST_WALLET_ADDRESS,
            TEST_WALLET_SIGNATURE,
            TEST_EWT,
            TEST_FIAT_VALUE,
            TEST_FIAT_CURRENCY,
            AdyenPaymentRepository.Methods.CREDIT_CARD.transactionType)
    )
        .thenReturn(Single.error(throwable))

    Mockito.`when`(mapper.mapInfoModelError(throwable))
        .thenReturn(model)
    val testObserver = TestObserver<PaymentInfoModel>()

    adyenRepo.loadPaymentInfo(
        AdyenPaymentRepository.Methods.CREDIT_CARD,
        TEST_FIAT_VALUE,
        TEST_FIAT_CURRENCY,
        TEST_WALLET_ADDRESS,
        TEST_WALLET_SIGNATURE,
        TEST_EWT
    )
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == model }
  }

//  @Test
//  fun submitRedirectTest() {
//    val testObserver = TestObserver<PaymentModel>()
//    val expectedResponse =
//        AdyenTransactionResponse(
//            TEST_UID, null, null, TransactionStatus.COMPLETED, null, null)
//    val expectedModel = PaymentModel(null, null, null, null, null, null,
//        TEST_UID, null, null, null,
//        emptyList(), PaymentModel.Status.COMPLETED, null, null)
//    Mockito.`when`(
//      adyenApi.submitRedirect(
//        TEST_UID,
//        TEST_WALLET_ADDRESS,
//        TEST_WALLET_SIGNATURE,
//        TEST_EWT,
//        AdyenPayment(JsonObject(), null)
//      )
//    )
//        .thenReturn(Single.just(expectedResponse))
//    Mockito.`when`(mapper.map(expectedResponse))
//        .thenReturn(expectedModel)
//    adyenRepo.submitRedirect(
//        TEST_UID,
//        TEST_WALLET_ADDRESS,
//        TEST_WALLET_SIGNATURE, JsonObject(), null)
//        .subscribe(testObserver)
//
//    testObserver.assertNoErrors()
//        .assertValue { it == expectedModel }
//  }

//  @Test
//  fun submitRedirectErrorTest() {
//    val testObserver = TestObserver<PaymentModel>()
//    val throwable = Throwable("Error")
//    val expectedModel = PaymentModel(Error())
//    Mockito.`when`(
//      adyenApi.submitRedirect(
//        TEST_UID,
//        TEST_WALLET_ADDRESS,
//        TEST_WALLET_SIGNATURE,
//        TEST_EWT,
//        AdyenPayment(JsonObject(), null)
//      )
//    )
//        .thenReturn(Single.error(throwable))
//    Mockito.`when`(mapper.mapPaymentModelError(throwable))
//        .thenReturn(expectedModel)
//    adyenRepo.submitRedirect(
//        TEST_UID,
//        TEST_WALLET_ADDRESS,
//        TEST_WALLET_SIGNATURE, JsonObject(), null)
//        .subscribe(testObserver)
//
//    testObserver.assertNoErrors()
//        .assertValue { it == expectedModel }
//  }

  @Test
  fun disablePaymentTest() {
    Mockito.`when`(
      adyenApi.disablePayments(
        DisableWallet(
          TEST_WALLET_ADDRESS
        )
      ))
        .thenReturn(Completable.complete())
    val testObserver = TestObserver<Boolean>()
    adyenRepo.disablePayments(
        TEST_WALLET_ADDRESS)
        .subscribe(testObserver)
    testObserver.assertNoErrors()
        .assertValue { it }
  }

  @Test
  fun disablePaymentErrorTest() {
    Mockito.`when`(
      adyenApi.disablePayments(
        DisableWallet(
          TEST_WALLET_ADDRESS
        )
      ))
        .thenReturn(Completable.error(Throwable("Error")))
    val testObserver = TestObserver<Boolean>()
    adyenRepo.disablePayments(
        TEST_WALLET_ADDRESS)
        .subscribe(testObserver)
    testObserver.assertNoErrors()
        .assertValue { !it }
  }

//  @Test
//  fun getTransactionTest() {
//    val testObserver = TestObserver<PaymentModel>()
//    val expectedTransaction =
//        Transaction(
//            TEST_UID, Transaction.Status.COMPLETED, Gateway(Gateway.Name.adyen_v2, "", ""),
//            null, null, null, null, "type", null)
//    val expectedPaymentModel =
//        PaymentModel(null, null, null, null, null, null,
//            TEST_UID, null, null, null,
//            emptyList(), PaymentModel.Status.COMPLETED)
//
//    Mockito.`when`(
//        brokerBdsApi.getAppcoinsTransaction(
//            TEST_UID,
//            TEST_WALLET_ADDRESS,
//            TEST_WALLET_SIGNATURE,
//            TEST_EWT
//        ))
//        .thenReturn(Single.just(expectedTransaction))
//    Mockito.`when`(mapper.map(expectedTransaction))
//        .thenReturn(expectedPaymentModel)
//
//    adyenRepo.getTransaction(
//        TEST_UID,
//        TEST_WALLET_ADDRESS,
//        TEST_WALLET_SIGNATURE)
//        .subscribe(testObserver)
//
//    testObserver.assertNoErrors()
//        .assertValue { it == expectedPaymentModel }
//  }

//  @Test
//  fun getTransactionErrorTest() {
//    val testObserver = TestObserver<PaymentModel>()
//    val throwable = Throwable("Error")
//    val expectedPaymentModel = PaymentModel(Error())
//
//    Mockito.`when`(
//        brokerBdsApi.getAppcoinsTransaction(
//            TEST_UID,
//            TEST_WALLET_ADDRESS,
//            TEST_WALLET_SIGNATURE,
//            TEST_EWT,
//        ))
//        .thenReturn(Single.error(throwable))
//    Mockito.`when`(mapper.mapPaymentModelError(throwable))
//        .thenReturn(expectedPaymentModel)
//
//    adyenRepo.getTransaction(
//        TEST_UID,
//        TEST_WALLET_ADDRESS,
//        TEST_WALLET_SIGNATURE)
//        .subscribe(testObserver)
//
//    testObserver.assertNoErrors()
//        .assertValue { it == expectedPaymentModel }
//  }

//  @Test
//  fun inAppMakePaymentTest() {
//    val testObserver = TestObserver<PaymentModel>()
//    val modelObject = CardPaymentMethod()
//    val paymentType = AdyenPaymentRepository.Methods.CREDIT_CARD.transactionType
//    val expectedAdyenTransactionResponse =
//        AdyenTransactionResponse(
//            TEST_UID, null, null, TransactionStatus.COMPLETED, null, null)
//    val expectedModel = PaymentModel(null, null, null, null, null, null,
//        TEST_UID, null, null, null,
//        emptyList(), PaymentModel.Status.COMPLETED, null, null)
//
//    Mockito.`when`(
//      adyenApi.makeAdyenPayment(
//        TEST_WALLET_ADDRESS,
//        TEST_WALLET_SIGNATURE,
//        TEST_EWT,
//        PaymentDetails(
//          modelObject, false, "", "Ecommerce", null, null, null,
//          null, paymentType, null,
//          null, null, BillingSupportedType.INAPP.name,
//          TEST_FIAT_CURRENCY,
//          TEST_FIAT_VALUE,
//          null, null, null, null, null, null
//        )
//      ))
//        .thenReturn(Single.just(expectedAdyenTransactionResponse))
//
//    Mockito.`when`(mapper.map(expectedAdyenTransactionResponse))
//        .thenReturn(expectedModel)
//
//    adyenRepo.makePayment(modelObject, false, false, emptyList(), "",
//        TEST_FIAT_VALUE,
//        TEST_FIAT_CURRENCY, null, paymentType,
//        TEST_WALLET_ADDRESS, null, null, null, null, null,
//        BillingSupportedType.INAPP.name, null, null, null, null, null,
//        TEST_WALLET_SIGNATURE, null, null)
//        .subscribe(testObserver)
//
//    testObserver.assertNoErrors()
//        .assertValue { it == expectedModel }
//  }

//  @Test
//  fun subscriptionMakePaymentTest() {
//    val testObserver = TestObserver<PaymentModel>()
//    val modelObject = CardPaymentMethod()
//    val paymentType = AdyenPaymentRepository.Methods.CREDIT_CARD.transactionType
//    val expectedAdyenTransactionResponse =
//        AdyenTransactionResponse(
//            TEST_UID, null, null, TransactionStatus.COMPLETED, null, null)
//    val expectedModel = PaymentModel(null, null, null, null, null, null,
//        TEST_UID, null, null, null,
//        emptyList(), PaymentModel.Status.COMPLETED, null, null)
//
//    Mockito.`when`(subscriptionsApi.getSkuSubscriptionToken("trivial drive", "sku",
//        TEST_FIAT_CURRENCY,
//        TEST_WALLET_ADDRESS,
//        TEST_WALLET_SIGNATURE,
//        TEST_EWT,
//    ))
//        .thenReturn(Single.just("token"))
//
//    Mockito.`when`(adyenApi.makeTokenPayment(TEST_WALLET_ADDRESS, TEST_WALLET_SIGNATURE, TEST_EWT,
//        TokenPayment(modelObject, false, "", "Ecommerce", null, null, null, paymentType, null, null,
//            null, null, null, null, null, null, "token")))
//        .thenReturn(Single.just(expectedAdyenTransactionResponse))
//
//    Mockito.`when`(ewtAuthenticatorService.getEwtAuthentication())
//      .thenReturn(Single.just(TEST_EWT))
//
//    Mockito.`when`(mapper.map(expectedAdyenTransactionResponse))
//        .thenReturn(expectedModel)
//
//    adyenRepo.makePayment(modelObject, false, false, emptyList(), "",
//        TEST_FIAT_VALUE,
//        TEST_FIAT_CURRENCY, null, paymentType,
//        TEST_WALLET_ADDRESS, null, "trivial drive", null,
//        "sku", null, BillingSupportedType.INAPP_SUBSCRIPTION.name, null, null, null, null, null,
//        TEST_WALLET_SIGNATURE, null, null)
//        .subscribe(testObserver)
//
//    testObserver.assertNoErrors()
//        .assertValue { it == expectedModel }
//  }

//  @Test
//  fun makePaymentErrorTest() {
//    val testObserver = TestObserver<PaymentModel>()
//    val modelObject = CardPaymentMethod()
//    val paymentType = AdyenPaymentRepository.Methods.CREDIT_CARD.transactionType
//    val throwable = Throwable("Error")
//    val expectedPaymentModel = PaymentModel(Error())
//
//
//    Mockito.`when`(subscriptionsApi.getSkuSubscriptionToken("trivial drive", "sku",
//        TEST_FIAT_CURRENCY,
//        TEST_WALLET_ADDRESS,
//        TEST_WALLET_SIGNATURE,
//        TEST_EWT
//    ))
//        .thenReturn(Single.just("token"))
//
//    Mockito.`when`(adyenApi.makeTokenPayment(TEST_WALLET_ADDRESS, TEST_WALLET_SIGNATURE, TEST_EWT,
//        TokenPayment(modelObject, false, "", "Ecommerce", null, null, null, paymentType, null, null,
//            null, null, null, null, null, null, "token")))
//        .thenReturn(Single.error(throwable))
//
//    Mockito.`when`(mapper.mapPaymentModelError(throwable))
//        .thenReturn(expectedPaymentModel)
//
//    adyenRepo.makePayment(modelObject, false, false, emptyList(), "",
//        TEST_FIAT_VALUE,
//        TEST_FIAT_CURRENCY, null, paymentType,
//        TEST_WALLET_ADDRESS, null, "trivial drive", null,
//        "sku", null, BillingSupportedType.INAPP_SUBSCRIPTION.name, null, null, null, null, null,
//        TEST_WALLET_SIGNATURE, null, null)
//        .subscribe(testObserver)
//
//    testObserver.assertNoErrors()
//        .assertValue { it == expectedPaymentModel }
//  }

//  @Test
//  fun makePaymentTokenErrorTest() {
//    val testObserver = TestObserver<PaymentModel>()
//    val modelObject = CardPaymentMethod()
//    val paymentType = AdyenPaymentRepository.Methods.CREDIT_CARD.transactionType
//    val throwable = Throwable("Error")
//    val expectedPaymentModel = PaymentModel(Error())
//
//
//    Mockito.`when`(subscriptionsApi.getSkuSubscriptionToken("trivial drive", "sku",
//        TEST_FIAT_CURRENCY,
//        TEST_WALLET_ADDRESS,
//        TEST_WALLET_SIGNATURE,
//        TEST_EWT
//    ))
//        .thenReturn(Single.error(throwable))
//
//    Mockito.`when`(mapper.mapPaymentModelError(throwable))
//        .thenReturn(expectedPaymentModel)
//
//    adyenRepo.makePayment(modelObject, false, false, emptyList(), "",
//        TEST_FIAT_VALUE,
//        TEST_FIAT_CURRENCY, null, paymentType,
//        TEST_WALLET_ADDRESS, null, "trivial drive", null,
//        "sku", null, BillingSupportedType.INAPP_SUBSCRIPTION.name, null, null, null, null, null,
//        TEST_WALLET_SIGNATURE, null, null)
//        .subscribe(testObserver)
//
//    testObserver.assertNoErrors()
//        .assertValue { it == expectedPaymentModel }
//  }
}
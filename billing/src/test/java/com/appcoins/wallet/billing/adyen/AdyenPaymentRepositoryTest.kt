package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.payments.request.CardPaymentMethod
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.bdsbilling.repository.entity.Gateway
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.bdsbilling.subscriptions.SubscriptionBillingApi
import com.appcoins.wallet.billing.common.response.TransactionStatus
import com.appcoins.wallet.billing.util.Error
import com.google.gson.JsonObject
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Assert
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
  lateinit var adyenApi: AdyenPaymentRepository.AdyenApi

  @Mock
  lateinit var bdsApi: RemoteRepository.BdsApi

  @Mock
  lateinit var subscriptionsApi: SubscriptionBillingApi

  @Mock
  lateinit var mapper: AdyenResponseMapper

  private lateinit var adyenRepo: AdyenPaymentRepository

  companion object {
    private const val TEST_WALLET_ADDRESS = "0x123"
    private const val TEST_WALLET_SIGNATURE = "0x987"
    private const val TEST_FIAT_VALUE = "2.0"
    private const val TEST_FIAT_CURRENCY = "EUR"
    private const val TEST_UID = "uid"
  }

  @Before
  fun setup() {
    adyenRepo = AdyenPaymentRepository(adyenApi, bdsApi, subscriptionsApi, mapper)
  }

  @Test
  fun loadPaymentInfoTest() {
    val response = PaymentMethodsResponse(Price(BigDecimal(2),
        TEST_FIAT_CURRENCY), JsonObject())

    val model = PaymentInfoModel(null, false, BigDecimal(2),
        TEST_FIAT_CURRENCY)
    Mockito.`when`(
        adyenApi.loadPaymentInfo(
            TEST_WALLET_ADDRESS,
            TEST_FIAT_VALUE,
            TEST_FIAT_CURRENCY,
            AdyenPaymentRepository.Methods.CREDIT_CARD.transactionType))
        .thenReturn(Single.just(response))

    Mockito.`when`(mapper.map(response, AdyenPaymentRepository.Methods.CREDIT_CARD))
        .thenReturn(model)
    val testObserver = TestObserver<PaymentInfoModel>()

    adyenRepo.loadPaymentInfo(AdyenPaymentRepository.Methods.CREDIT_CARD,
        TEST_FIAT_VALUE,
        TEST_FIAT_CURRENCY,
        TEST_WALLET_ADDRESS)
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
            TEST_FIAT_VALUE,
            TEST_FIAT_CURRENCY,
            AdyenPaymentRepository.Methods.CREDIT_CARD.transactionType))
        .thenReturn(Single.error(throwable))

    Mockito.`when`(mapper.mapInfoModelError(throwable))
        .thenReturn(model)
    val testObserver = TestObserver<PaymentInfoModel>()

    adyenRepo.loadPaymentInfo(AdyenPaymentRepository.Methods.CREDIT_CARD,
        TEST_FIAT_VALUE,
        TEST_FIAT_CURRENCY,
        TEST_WALLET_ADDRESS)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == model }
  }

  @Test
  fun makeVerificationPaymentTest() {
    val expectedModel = VerificationPaymentModel(true)

    val model = CardPaymentMethod()
    Mockito.`when`(
        adyenApi.makeVerificationPayment(
            TEST_WALLET_ADDRESS,
            TEST_WALLET_SIGNATURE,
            AdyenPaymentRepository.VerificationPayment(model, false, "")))
        .thenReturn(Completable.complete())

    Mockito.`when`(mapper.mapVerificationPaymentModelSuccess(it))
        .thenReturn(VerificationPaymentModel(true))

    val testObserver = TestObserver<VerificationPaymentModel>()
    adyenRepo.makeVerificationPayment(model, false, "",
        TEST_WALLET_ADDRESS,
        TEST_WALLET_SIGNATURE)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == expectedModel }
  }

  @Test
  fun makeVerificationPaymentErrorTest() {
    val expectedModel = VerificationPaymentModel(false, VerificationPaymentModel.ErrorType.OTHER)
    val throwable = Throwable("error")
    val modelObject = CardPaymentMethod()
    Mockito.`when`(
        adyenApi.makeVerificationPayment(
            TEST_WALLET_ADDRESS,
            TEST_WALLET_SIGNATURE,
            AdyenPaymentRepository.VerificationPayment(modelObject, false, "")))
        .thenReturn(Completable.error(throwable))

    Mockito.`when`(mapper.mapVerificationPaymentModelError(throwable))
        .thenReturn(VerificationPaymentModel(false, VerificationPaymentModel.ErrorType.OTHER))

    val testObserver = TestObserver<VerificationPaymentModel>()
    adyenRepo.makeVerificationPayment(modelObject, false, "",
        TEST_WALLET_ADDRESS,
        TEST_WALLET_SIGNATURE)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == expectedModel }
  }

  @Test
  fun getVerificationInfoTest() {
    val expectedResponse =
        VerificationInfoResponse(
            TEST_FIAT_CURRENCY, "$",
            TEST_FIAT_VALUE, 6, "", "")
    Mockito.`when`(
        adyenApi.getVerificationInfo(
            TEST_WALLET_ADDRESS,
            TEST_WALLET_SIGNATURE))
        .thenReturn(Single.just(expectedResponse))
    val response = adyenRepo.getVerificationInfo(
        TEST_WALLET_ADDRESS,
        TEST_WALLET_SIGNATURE)
        .blockingGet()
    Assert.assertEquals(expectedResponse, response)
  }

  @Test
  fun validateCodeTest() {
    val expectedResult = VerificationCodeResult(true)
    Mockito.`when`(
        adyenApi.validateCode(
            TEST_WALLET_ADDRESS,
            TEST_WALLET_SIGNATURE, "code"))
        .thenReturn(Completable.complete())

    val testObserver = TestObserver<VerificationCodeResult>()

    adyenRepo.validateCode("code",
        TEST_WALLET_ADDRESS,
        TEST_WALLET_SIGNATURE)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == expectedResult }
  }

  @Test
  fun validateCodeErrorTest() {
    val expectedResult = VerificationCodeResult(false)
    val throwable = Throwable("error")
    val code = "code"
    Mockito.`when`(adyenApi.validateCode(
        TEST_WALLET_ADDRESS,
        TEST_WALLET_SIGNATURE, code))
        .thenReturn(Completable.error(throwable))
    Mockito.`when`(mapper.mapVerificationCodeError(throwable))
        .thenReturn(expectedResult)

    val testObserver = TestObserver<VerificationCodeResult>()

    adyenRepo.validateCode(code,
        TEST_WALLET_ADDRESS,
        TEST_WALLET_SIGNATURE)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == expectedResult }
  }

  @Test
  fun submitRedirectTest() {
    val testObserver = TestObserver<PaymentModel>()
    val expectedResponse =
        AdyenTransactionResponse(
            TEST_UID, null, null, TransactionStatus.COMPLETED, null, null)
    val expectedModel = PaymentModel(null, null, null, null, null, null,
        TEST_UID, null, null, null,
        emptyList(), PaymentModel.Status.COMPLETED, null, null)
    Mockito.`when`(
        adyenApi.submitRedirect(
            TEST_UID,
            TEST_WALLET_ADDRESS,
            TEST_WALLET_SIGNATURE,
            AdyenPaymentRepository.AdyenPayment(JsonObject(), null)))
        .thenReturn(Single.just(expectedResponse))
    Mockito.`when`(mapper.map(expectedResponse))
        .thenReturn(expectedModel)
    adyenRepo.submitRedirect(
        TEST_UID,
        TEST_WALLET_ADDRESS,
        TEST_WALLET_SIGNATURE, JsonObject(), null)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == expectedModel }
  }

  @Test
  fun submitRedirectErrorTest() {
    val testObserver = TestObserver<PaymentModel>()
    val throwable = Throwable("Error")
    val expectedModel = PaymentModel(Error())
    Mockito.`when`(adyenApi.submitRedirect(
        TEST_UID,
        TEST_WALLET_ADDRESS,
        TEST_WALLET_SIGNATURE,
        AdyenPaymentRepository.AdyenPayment(JsonObject(), null)))
        .thenReturn(Single.error(throwable))
    Mockito.`when`(mapper.mapPaymentModelError(throwable))
        .thenReturn(expectedModel)
    adyenRepo.submitRedirect(
        TEST_UID,
        TEST_WALLET_ADDRESS,
        TEST_WALLET_SIGNATURE, JsonObject(), null)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == expectedModel }
  }

  @Test
  fun disablePaymentTest() {
    Mockito.`when`(
        adyenApi.disablePayments(AdyenPaymentRepository.DisableWallet(
            TEST_WALLET_ADDRESS)))
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
        adyenApi.disablePayments(AdyenPaymentRepository.DisableWallet(
            TEST_WALLET_ADDRESS)))
        .thenReturn(Completable.error(Throwable("Error")))
    val testObserver = TestObserver<Boolean>()
    adyenRepo.disablePayments(
        TEST_WALLET_ADDRESS)
        .subscribe(testObserver)
    testObserver.assertNoErrors()
        .assertValue { !it }
  }

  @Test
  fun getTransactionTest() {
    val testObserver = TestObserver<PaymentModel>()
    val expectedTransaction =
        Transaction(
            TEST_UID, Transaction.Status.COMPLETED, Gateway(Gateway.Name.adyen_v2, "", ""),
            null, null, null, null, "type", null)
    val expectedPaymentModel =
        PaymentModel(null, null, null, null, null, null,
            TEST_UID, null, null, null,
            emptyList(), PaymentModel.Status.COMPLETED)

    Mockito.`when`(
        bdsApi.getAppcoinsTransaction(
            TEST_UID,
            TEST_WALLET_ADDRESS,
            TEST_WALLET_SIGNATURE))
        .thenReturn(Single.just(expectedTransaction))
    Mockito.`when`(mapper.map(expectedTransaction))
        .thenReturn(expectedPaymentModel)

    adyenRepo.getTransaction(
        TEST_UID,
        TEST_WALLET_ADDRESS,
        TEST_WALLET_SIGNATURE)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == expectedPaymentModel }
  }

  @Test
  fun getTransactionErrorTest() {
    val testObserver = TestObserver<PaymentModel>()
    val throwable = Throwable("Error")
    val expectedPaymentModel = PaymentModel(Error())

    Mockito.`when`(
        bdsApi.getAppcoinsTransaction(
            TEST_UID,
            TEST_WALLET_ADDRESS,
            TEST_WALLET_SIGNATURE))
        .thenReturn(Single.error(throwable))
    Mockito.`when`(mapper.mapPaymentModelError(throwable))
        .thenReturn(expectedPaymentModel)

    adyenRepo.getTransaction(
        TEST_UID,
        TEST_WALLET_ADDRESS,
        TEST_WALLET_SIGNATURE)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == expectedPaymentModel }
  }

  @Test
  fun inAppMakePaymentTest() {
    val testObserver = TestObserver<PaymentModel>()
    val modelObject = CardPaymentMethod()
    val paymentType = AdyenPaymentRepository.Methods.CREDIT_CARD.transactionType
    val expectedAdyenTransactionResponse =
        AdyenTransactionResponse(
            TEST_UID, null, null, TransactionStatus.COMPLETED, null, null)
    val expectedModel = PaymentModel(null, null, null, null, null, null,
        TEST_UID, null, null, null,
        emptyList(), PaymentModel.Status.COMPLETED, null, null)

    Mockito.`when`(
        adyenApi.makePayment(
            TEST_WALLET_ADDRESS,
            TEST_WALLET_SIGNATURE,
            AdyenPaymentRepository.Payment(modelObject, false, "", "Ecommerce", null, null, null,
                null, paymentType, null,
                null, null, BillingSupportedType.INAPP.name,
                TEST_FIAT_CURRENCY,
                TEST_FIAT_VALUE,
                null, null, null, null, null)))
        .thenReturn(Single.just(expectedAdyenTransactionResponse))

    Mockito.`when`(mapper.map(expectedAdyenTransactionResponse))
        .thenReturn(expectedModel)

    adyenRepo.makePayment(modelObject, false, false, emptyList(), "",
        TEST_FIAT_VALUE,
        TEST_FIAT_CURRENCY, null, paymentType,
        TEST_WALLET_ADDRESS, null, null, null, null, null,
        BillingSupportedType.INAPP.name, null, null, null, null,
        TEST_WALLET_SIGNATURE, null, null)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == expectedModel }
  }

  @Test
  fun subscriptionMakePaymentTest() {
    val testObserver = TestObserver<PaymentModel>()
    val modelObject = CardPaymentMethod()
    val paymentType = AdyenPaymentRepository.Methods.CREDIT_CARD.transactionType
    val expectedAdyenTransactionResponse =
        AdyenTransactionResponse(
            TEST_UID, null, null, TransactionStatus.COMPLETED, null, null)
    val expectedModel = PaymentModel(null, null, null, null, null, null,
        TEST_UID, null, null, null,
        emptyList(), PaymentModel.Status.COMPLETED, null, null)

    Mockito.`when`(subscriptionsApi.getSkuSubscriptionToken("trivial drive", "sku",
        TEST_FIAT_CURRENCY,
        TEST_WALLET_ADDRESS,
        TEST_WALLET_SIGNATURE))
        .thenReturn(Single.just("token"))

    Mockito.`when`(
        adyenApi.makeTokenPayment(
            TEST_WALLET_ADDRESS,
            TEST_WALLET_SIGNATURE,
            TokenPayment(modelObject, false, "", "Ecommerce", null, null, null,
                paymentType, null,
                null, null, null, null, null, null, "token")))
        .thenReturn(Single.just(expectedAdyenTransactionResponse))

    Mockito.`when`(mapper.map(expectedAdyenTransactionResponse))
        .thenReturn(expectedModel)

    adyenRepo.makePayment(modelObject, false, false, emptyList(), "",
        TEST_FIAT_VALUE,
        TEST_FIAT_CURRENCY, null, paymentType,
        TEST_WALLET_ADDRESS, null, "trivial drive", null,
        "sku", null, BillingSupportedType.INAPP_SUBSCRIPTION.name, null, null, null, null,
        TEST_WALLET_SIGNATURE, null, null)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == expectedModel }
  }

  @Test
  fun makePaymentErrorTest() {
    val testObserver = TestObserver<PaymentModel>()
    val modelObject = CardPaymentMethod()
    val paymentType = AdyenPaymentRepository.Methods.CREDIT_CARD.transactionType
    val throwable = Throwable("Error")
    val expectedPaymentModel = PaymentModel(Error())


    Mockito.`when`(subscriptionsApi.getSkuSubscriptionToken("trivial drive", "sku",
        TEST_FIAT_CURRENCY,
        TEST_WALLET_ADDRESS,
        TEST_WALLET_SIGNATURE))
        .thenReturn(Single.just("token"))

    Mockito.`when`(
        adyenApi.makeTokenPayment(
            TEST_WALLET_ADDRESS,
            TEST_WALLET_SIGNATURE,
            TokenPayment(modelObject, false, "", "Ecommerce", null, null, null,
                paymentType, null,
                null, null, null, null, null, null, "token")))
        .thenReturn(Single.error(throwable))

    Mockito.`when`(mapper.mapPaymentModelError(throwable))
        .thenReturn(expectedPaymentModel)

    adyenRepo.makePayment(modelObject, false, false, emptyList(), "",
        TEST_FIAT_VALUE,
        TEST_FIAT_CURRENCY, null, paymentType,
        TEST_WALLET_ADDRESS, null, "trivial drive", null,
        "sku", null, BillingSupportedType.INAPP_SUBSCRIPTION.name, null, null, null, null,
        TEST_WALLET_SIGNATURE, null, null)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == expectedPaymentModel }
  }

  @Test
  fun makePaymentTokenErrorTest() {
    val testObserver = TestObserver<PaymentModel>()
    val modelObject = CardPaymentMethod()
    val paymentType = AdyenPaymentRepository.Methods.CREDIT_CARD.transactionType
    val throwable = Throwable("Error")
    val expectedPaymentModel = PaymentModel(Error())


    Mockito.`when`(subscriptionsApi.getSkuSubscriptionToken("trivial drive", "sku",
        TEST_FIAT_CURRENCY,
        TEST_WALLET_ADDRESS,
        TEST_WALLET_SIGNATURE))
        .thenReturn(Single.error(throwable))

    Mockito.`when`(mapper.mapPaymentModelError(throwable))
        .thenReturn(expectedPaymentModel)

    adyenRepo.makePayment(modelObject, false, false, emptyList(), "",
        TEST_FIAT_VALUE,
        TEST_FIAT_CURRENCY, null, paymentType,
        TEST_WALLET_ADDRESS, null, "trivial drive", null,
        "sku", null, BillingSupportedType.INAPP_SUBSCRIPTION.name, null, null, null, null,
        TEST_WALLET_SIGNATURE, null, null)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == expectedPaymentModel }
  }
}
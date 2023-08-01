package com.asfoundation.wallet.adyen

import android.os.Bundle
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.appcoins.wallet.bdsbilling.WalletAddressModel
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.feature.promocode.data.repository.PromoCode
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.appcoins.wallet.feature.walletInfo.data.verification.WalletVerificationInteractor
import com.asfoundation.wallet.billing.address.BillingAddressRepository
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.billing.adyen.PurchaseBundleModel
import com.asfoundation.wallet.billing.partners.AttributionEntity
import com.asfoundation.wallet.billing.partners.PartnerAddressService
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.wallet.appcoins.feature.support.data.SupportInteractor
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.util.FakeSchedulers
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.google.gson.JsonObject
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import okhttp3.internal.wait
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class AdyenPaymentInteractorTest {

  companion object {
    private const val TEST_WALLET_ADDRESS = "0x123"
    private const val TEST_WALLET_SIGNATURE = "0x1234"
    private const val TEST_EWT = "123456789"
    private const val TEST_FIAT_VALUE = "2.00"
    private const val TEST_FIAT_CURRENCY = "EUR"
  }

  @Mock
  lateinit var repository: AdyenPaymentRepository

  @Mock
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  @Mock
  lateinit var billingMessageMapper: BillingMessagesMapper

  @Mock
  lateinit var partnerAddressService: PartnerAddressService

  @Mock
  lateinit var walletService: WalletService

  @Mock
  lateinit var supportInteractor: SupportInteractor

  @Mock
  lateinit var walletBlockedInteractor: WalletBlockedInteract

  @Mock
  lateinit var walletVerificationInteractor: WalletVerificationInteractor

  @Mock
  lateinit var billingAddressRepository: BillingAddressRepository

  @Mock
  lateinit var getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase

  @Mock
  lateinit var ewtObtainer: EwtAuthenticatorService

  private lateinit var interactor: AdyenPaymentInteractor
  private val fakeSchedulers = FakeSchedulers()

  @Before
  fun setup() {
    interactor = AdyenPaymentInteractor(repository, inAppPurchaseInteractor, billingMessageMapper,
        partnerAddressService, walletService, supportInteractor, walletBlockedInteractor,
        walletVerificationInteractor, billingAddressRepository, getCurrentPromoCodeUseCase,
        ewtObtainer, fakeSchedulers)
  }

  @Test
  fun forgetBillingAddressTest() {
    interactor.forgetBillingAddress()
    Mockito.verify(billingAddressRepository)
        .forgetBillingAddress()
  }

  @Test
  fun isWalletBlockedTest() {
    val testObserver = TestObserver<Boolean>()
    Mockito.`when`(walletBlockedInteractor.isWalletBlocked())
        .thenReturn(Single.just(true))

    interactor.isWalletBlocked()
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it }
  }

  @Test
  fun isWalletVerifiedTest() {
    val testObserver = TestObserver<Boolean>()
    Mockito.`when`(walletService.getAndSignCurrentWalletAddress())
        .thenReturn(Single.just(WalletAddressModel(TEST_WALLET_ADDRESS, TEST_WALLET_SIGNATURE)))
    Mockito.`when`(walletVerificationInteractor.isVerified(TEST_WALLET_ADDRESS,
        TEST_WALLET_SIGNATURE))
        .thenReturn(Single.just(false))

    interactor.isWalletVerified()
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { !it }
  }

  @Test
  fun isWalletVerifiedErrorTest() {
    val testObserver = TestObserver<Boolean>()
    Mockito.`when`(walletService.getAndSignCurrentWalletAddress())
        .thenReturn(Single.just(WalletAddressModel(TEST_WALLET_ADDRESS, TEST_WALLET_SIGNATURE)))
    Mockito.`when`(
        walletVerificationInteractor.isVerified(TEST_WALLET_ADDRESS, TEST_WALLET_SIGNATURE))
        .thenReturn(Single.error(Throwable("Error")))

    interactor.isWalletVerified()
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it }
  }

  @Test
  fun showSupportTest() {
    val testObserver = TestObserver<Any>()
    Mockito.`when`(supportInteractor.showSupport(1))
        .thenReturn(Completable.complete())

    interactor.showSupport(1)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertComplete()
  }

  @Test
  fun loadPaymentInfoTest() {
    val testObserver = TestObserver<PaymentInfoModel>()
    val expectedModel = PaymentInfoModel(null, false, BigDecimal(2), TEST_FIAT_CURRENCY)
    Mockito.`when`(walletService.getWalletAddress())
        .thenReturn(
          Single.just(TEST_WALLET_ADDRESS)
        )
    Mockito.`when`(ewtObtainer.getEwtAuthentication())
      .thenReturn(
        Single.just(TEST_EWT)
      )

    interactor.loadPaymentInfo(AdyenPaymentRepository.Methods.CREDIT_CARD,
        TEST_FIAT_VALUE, TEST_FIAT_CURRENCY)
        .subscribe(testObserver)

    testObserver.awaitDone(1, TimeUnit.SECONDS)
      .assertNoErrors()
  }

  @Test
  fun makePaymentTest() {
    val testObserver = TestObserver<PaymentModel>()
    val expectedModel =
        PaymentModel(null, null, null, null, null, "", "uid", null, null, null, emptyList(),
            PaymentModel.Status.COMPLETED, null, null)
    val payment = CardPaymentMethod()
    Mockito.`when`(walletService.getAndSignCurrentWalletAddress())
        .thenReturn(Single.just(WalletAddressModel(TEST_WALLET_ADDRESS, TEST_WALLET_SIGNATURE)))
    Mockito.`when`(partnerAddressService.getAttributionEntity("package"))
        .thenReturn(Single.just(AttributionEntity("store_address", "oem_address")))
    Mockito.`when`(getCurrentPromoCodeUseCase())
        .thenReturn(Single.just(PromoCode(null, null, null, null)))
    Mockito.`when`(repository.makePayment(payment, false, false, emptyList(), "", TEST_FIAT_VALUE,
        TEST_FIAT_CURRENCY, null, "", TEST_WALLET_ADDRESS, "", "package", null, "sku", null,
        "INAPP", null, "store_address", "oem_address", null, TEST_WALLET_ADDRESS,
        TEST_WALLET_SIGNATURE, null, null))
        .thenReturn(Single.just(expectedModel))

    interactor.makePayment(payment, false, false, emptyList(), "", TEST_FIAT_VALUE,
        TEST_FIAT_CURRENCY, null, "", "", "package", null, "sku", null, "INAPP", null, null, null)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == expectedModel }
  }

  @Test
  fun makeTopUpPaymentTest() {
    val testObserver = TestObserver<PaymentModel>()
    val expectedModel =
        PaymentModel(null, null, null, null, null, "", "uid", null, null, null, emptyList(),
            PaymentModel.Status.COMPLETED, null, null)
    val payment = CardPaymentMethod()
    Mockito.`when`(walletService.getAndSignCurrentWalletAddress())
        .thenReturn(Single.just(WalletAddressModel(TEST_WALLET_ADDRESS, TEST_WALLET_SIGNATURE)))
    Mockito.`when`(repository.makePayment(payment, false, false, emptyList(), "", TEST_FIAT_VALUE,
        TEST_FIAT_CURRENCY, null, "", TEST_WALLET_ADDRESS, null, "wallet", null, null, null,
        "TOPUP", null, null, null, null, null, TEST_WALLET_SIGNATURE, null, null))
        .thenReturn(Single.just(expectedModel))

    interactor.makeTopUpPayment(payment, false, false, emptyList(), "", TEST_FIAT_VALUE,
        TEST_FIAT_CURRENCY, "", "TOPUP", "wallet", null)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == expectedModel }
  }

  @Test
  fun submitRedirectTest() {
    val testObserver = TestObserver<PaymentModel>()
    val json = JsonObject()
    val expectedModel =
        PaymentModel(null, null, null, null, null, "", "uid", null, null, null, emptyList(),
            PaymentModel.Status.COMPLETED, null, null)
    Mockito.`when`(walletService.getWalletAddress())
        .thenReturn(Single.just(TEST_WALLET_ADDRESS))
    Mockito.`when`(
        repository.submitRedirect("uid", TEST_WALLET_ADDRESS, json, null))
        .thenReturn(Single.just(expectedModel))

    interactor.submitRedirect("uid", json, null)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == expectedModel }
  }

  @Test
  fun disablePaymentTest() {
    val testObserver = TestObserver<Boolean>()
    Mockito.`when`(walletService.getWalletAddress())
        .thenReturn(Single.just(TEST_WALLET_ADDRESS))
    Mockito.`when`(repository.disablePayments(TEST_WALLET_ADDRESS))
        .thenReturn(Single.just(true))

    interactor.disablePayments()
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it }
  }

  @Test
  fun convertToFiatTest() {
    val testObserver = TestObserver<FiatValue>()
    val expectedFiatValue = FiatValue(
      BigDecimal(2),
      TEST_FIAT_CURRENCY
    )
    Mockito.`when`(inAppPurchaseInteractor.convertToFiat(2.0, TEST_FIAT_CURRENCY))
        .thenReturn(Single.just(expectedFiatValue))

    interactor.convertToFiat(2.0, TEST_FIAT_CURRENCY)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == expectedFiatValue }
  }

  @Test
  fun mapCancellationTest() {
    val expectedBundle = Bundle()
    Mockito.`when`(billingMessageMapper.mapCancellation())
        .thenReturn(expectedBundle)
    val bundle = interactor.mapCancellation()
    Assert.assertEquals(bundle, expectedBundle)
  }

  @Test
  fun removePreselectedMethodTest() {
    interactor.removePreSelectedPaymentMethod()
    Mockito.verify(inAppPurchaseInteractor)
        .removePreSelectedPaymentMethod()
  }

  @Test
  fun getCompletedPurchaseBundle() {
    val testObserver = TestObserver<PurchaseBundleModel>()
    val expectedModel = PurchaseBundleModel(Bundle())
    Mockito.`when`(
        inAppPurchaseInteractor.getCompletedPurchaseBundle("INAPP", "merchant", "sku", null, null,
            null, fakeSchedulers.main))
        .thenReturn(Single.just(expectedModel))
    interactor.getCompletePurchaseBundle("INAPP", "merchant", "sku", null, null, null,
        fakeSchedulers.main)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == expectedModel }
  }

  @Test
  fun convertToLocalFiatTest() {
    val testObserver = TestObserver<FiatValue>()
    val expectedFiatValue = FiatValue(
      BigDecimal(2),
      TEST_FIAT_CURRENCY
    )
    Mockito.`when`(inAppPurchaseInteractor.convertToLocalFiat(2.0))
        .thenReturn(Single.just(expectedFiatValue))

    interactor.convertToLocalFiat(2.0)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertValue { it == expectedFiatValue }
  }

  @Test
  fun getAuthorisedTransactionTest() {
    val testObserver = TestObserver<PaymentModel>()
    val expectedModel =
        PaymentModel(null, null, null, null, null, null, "uid", null, null, null, emptyList(),
            PaymentModel.Status.COMPLETED, null, null)
    Mockito.`when`(walletService.getAndSignCurrentWalletAddress())
        .thenReturn(Single.just(WalletAddressModel(TEST_WALLET_ADDRESS, TEST_WALLET_SIGNATURE)))
    Mockito.`when`(repository.getTransaction("uid", TEST_WALLET_ADDRESS, TEST_WALLET_SIGNATURE))
        .thenReturn(Single.just(expectedModel))
    interactor.getAuthorisedTransaction("uid")
        .subscribe(testObserver)
    fakeSchedulers.testScheduler.advanceTimeTo(10, TimeUnit.SECONDS)
    testObserver.assertNoErrors()
        .assertValue { it == expectedModel }
  }

  @Test
  fun getFailedTransactionReasonTest() {
    val testObserver = TestObserver<PaymentModel>()
    val expectedModel =
        PaymentModel(null, null, null, null, null, null, "uid", null, null, null, emptyList(),
            PaymentModel.Status.COMPLETED, null, 20)
    Mockito.`when`(walletService.getAndSignCurrentWalletAddress())
        .thenReturn(Single.just(WalletAddressModel(TEST_WALLET_ADDRESS, TEST_WALLET_SIGNATURE)))
    Mockito.`when`(repository.getTransaction("uid", TEST_WALLET_ADDRESS, TEST_WALLET_SIGNATURE))
        .thenReturn(Single.just(expectedModel))
    interactor.getFailedTransactionReason("uid")
        .subscribe(testObserver)
    fakeSchedulers.testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)
    testObserver.assertNoErrors()
        .assertValue { it == expectedModel }
  }

  @Test
  fun getFailedTransactionReasonRepeatTest() {
    val testObserver = TestObserver<PaymentModel>()
    val expectedSuccessModel =
        PaymentModel(null, null, null, null, null, null, "uid", null, null, null, emptyList(),
            PaymentModel.Status.COMPLETED, null, 20)
    val expectedFailModel =
        PaymentModel(null, null, null, null, null, null, "uid", null, null, null, emptyList(),
            PaymentModel.Status.COMPLETED, null, null)
    Mockito.`when`(walletService.getAndSignCurrentWalletAddress())
        .thenReturn(Single.just(WalletAddressModel(TEST_WALLET_ADDRESS, TEST_WALLET_SIGNATURE)))
    Mockito.`when`(repository.getTransaction("uid", TEST_WALLET_ADDRESS, TEST_WALLET_SIGNATURE))
        .thenReturn(Single.just(expectedFailModel), Single.just(expectedSuccessModel))
    interactor.getFailedTransactionReason("uid")
        .subscribe(testObserver)
    fakeSchedulers.testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)
    fakeSchedulers.testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)
    testObserver.assertNoErrors()
        .assertValue { it == expectedSuccessModel }
  }
}
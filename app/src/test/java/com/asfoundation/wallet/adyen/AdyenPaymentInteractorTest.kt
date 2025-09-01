package com.asfoundation.wallet.adyen

import android.os.Bundle
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.analytics.analytics.partners.AttributionEntity
import com.appcoins.wallet.core.analytics.analytics.partners.PartnerAddressService
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.core.walletservices.WalletServices.WalletAddressModel
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.feature.promocode.data.repository.PromoCode
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationType
import com.appcoins.wallet.feature.walletInfo.data.verification.WalletVerificationInteractor
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.billing.adyen.PurchaseBundleModel
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.util.FakeSchedulers
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.google.gson.JsonObject
import com.wallet.appcoins.feature.support.data.SupportInteractor
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
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class AdyenPaymentInteractorTest {

  companion object {
    private const val TEST_WALLET_ADDRESS = "0x123"
    private const val TEST_WALLET_SIGNATURE = "0x1234"
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
  lateinit var getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase

  private lateinit var interactor: AdyenPaymentInteractor
  private val fakeSchedulers = FakeSchedulers()

  @Before
  fun setup() {
    interactor = AdyenPaymentInteractor(
      adyenPaymentRepository = repository,
      inAppPurchaseInteractor = inAppPurchaseInteractor,
      billingMessagesMapper = billingMessageMapper,
      partnerAddressService = partnerAddressService,
      walletService = walletService,
      supportInteractor = supportInteractor,
      walletBlockedInteract = walletBlockedInteractor,
      walletVerificationInteractor = walletVerificationInteractor,
      getCurrentPromoCodeUseCase = getCurrentPromoCodeUseCase,
      rxSchedulers = fakeSchedulers
    )
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
      .thenReturn(Single.just(WalletAddressModel(
        address = TEST_WALLET_ADDRESS,
        signedAddress = TEST_WALLET_SIGNATURE
      )))
    Mockito.`when`(
      walletVerificationInteractor.isVerified(
        address = TEST_WALLET_ADDRESS,
        type = VerificationType.CREDIT_CARD
      )
    )
      .thenReturn(Single.just(false))

    interactor.isWalletVerified(VerificationType.CREDIT_CARD)
      .subscribe(testObserver)

    testObserver.assertNoErrors()
      .assertValue { !it }
  }

  @Test
  fun isWalletVerifiedErrorTest() {
    val testObserver = TestObserver<Boolean>()
    Mockito.`when`(walletService.getAndSignCurrentWalletAddress())
      .thenReturn(Single.just(WalletAddressModel(
        address = TEST_WALLET_ADDRESS,
        signedAddress = TEST_WALLET_SIGNATURE
      )))
    Mockito.`when`(
      walletVerificationInteractor.isVerified(
        address = TEST_WALLET_ADDRESS,
        type = VerificationType.CREDIT_CARD
      )
    )
      .thenReturn(Single.error(Throwable("Error")))

    interactor.isWalletVerified(VerificationType.CREDIT_CARD)
      .subscribe(testObserver)

    testObserver.assertNoErrors()
      .assertValue { it }
  }

  @Test
  fun showSupportTest() {
    val testObserver = TestObserver<Any>()
    Mockito.`when`(supportInteractor.showSupport(""))
      .thenReturn(Completable.complete())

    interactor.showSupport("")
      .subscribe(testObserver)

    testObserver.assertNoErrors()
      .assertComplete()
  }

  @Test
  fun loadPaymentInfoTest() {
    val testObserver = TestObserver<PaymentInfoModel>()
    Mockito.`when`(walletService.getWalletAddress())
      .thenReturn(
        Single.just(TEST_WALLET_ADDRESS)
      )

    interactor.loadPaymentInfo(
      methods = AdyenPaymentRepository.Methods.CREDIT_CARD,
      value = TEST_FIAT_VALUE,
      currency = TEST_FIAT_CURRENCY
    )
      .subscribe(testObserver)

    testObserver.awaitDone(1, TimeUnit.SECONDS)
      .assertNoErrors()
  }

  @Test
  fun makePaymentTest() {
    val testObserver = TestObserver<PaymentModel>()
    val expectedModel =
      PaymentModel(
        resultCode = null,
        refusalReason = null,
        refusalCode = null,
        action = null,
        redirectUrl = null,
        paymentData = "",
        uid = "uid",
        purchaseUid = null,
        hash = null,
        orderReference = null,
        fraudResultIds = emptyList(),
        status = PaymentModel.Status.COMPLETED,
        errorMessage = null,
        errorCode = null
      )
    val payment = CardPaymentMethod()
    Mockito.`when`(walletService.getAndSignCurrentWalletAddress())
      .thenReturn(Single.just(WalletAddressModel(
        address = TEST_WALLET_ADDRESS,
        signedAddress = TEST_WALLET_SIGNATURE
      )))
    Mockito.`when`(partnerAddressService.getAttribution("package"))
      .thenReturn(Single.just(AttributionEntity("store_address", "oem_address")))
    Mockito.`when`(getCurrentPromoCodeUseCase())
      .thenReturn(Single.just(PromoCode(null, null, null, null)))
    Mockito.`when`(
      repository.makePayment(
        adyenPaymentMethod = payment,
        shouldStoreMethod = false,
        hasCvc = false,
        supportedShopperInteractions = emptyList(),
        returnUrl = "",
        value = TEST_FIAT_VALUE,
        currency = TEST_FIAT_CURRENCY,
        reference = null,
        paymentType = "",
        walletAddress = TEST_WALLET_ADDRESS,
        origin = "",
        packageName = "package",
        metadata = null,
        sku = "sku",
        callbackUrl = null,
        transactionType = "INAPP",
        entityOemId = "store_address",
        entityDomain = "oem_address",
        entityPromoCode = null,
        userWallet = TEST_WALLET_ADDRESS,
        referrerUrl = null,
        guestWalletId = null,
        externalBuyerReference = null,
        isFreeTrial = null
      )
    )
      .thenReturn(Single.just(expectedModel))

    interactor.makePayment(
      adyenPaymentMethod = payment,
      shouldStoreMethod = false,
      hasCvc = false,
      supportedShopperInteraction = emptyList(),
      returnUrl = "",
      value = TEST_FIAT_VALUE,
      currency = TEST_FIAT_CURRENCY,
      reference = null,
      paymentType = "",
      origin = "",
      packageName = "package",
      metadata = null,
      sku = "sku",
      callbackUrl = null,
      transactionType = "INAPP",
      referrerUrl = null,
      guestWalletId = null,
      externalBuyerReference = null,
      isFreeTrial = null,
    )
      .subscribe(testObserver)

    testObserver.assertNoErrors()
      .assertValue { it == expectedModel }
  }

  @Test
  fun makeTopUpPaymentTest() {
    val testObserver = TestObserver<PaymentModel>()
    val expectedModel =
      PaymentModel(
        resultCode = null,
        refusalReason = null,
        refusalCode = null,
        action = null,
        redirectUrl = null,
        paymentData = "",
        uid = "uid",
        purchaseUid = null,
        hash = null,
        orderReference = null,
        fraudResultIds = emptyList(),
        status = PaymentModel.Status.COMPLETED,
        errorMessage = null,
        errorCode = null
      )
    val payment = CardPaymentMethod()
    Mockito.`when`(walletService.getAndSignCurrentWalletAddress())
      .thenReturn(Single.just(WalletAddressModel(
        address = TEST_WALLET_ADDRESS,
        signedAddress = TEST_WALLET_SIGNATURE
      )))
    Mockito.`when`(
      repository.makePayment(
        adyenPaymentMethod = payment,
        shouldStoreMethod = false,
        hasCvc = false,
        supportedShopperInteractions = emptyList(),
        returnUrl = "",
        value = TEST_FIAT_VALUE,
        currency = TEST_FIAT_CURRENCY,
        reference = null,
        paymentType = "",
        walletAddress = TEST_WALLET_ADDRESS,
        origin = null,
        packageName = "wallet",
        metadata = null,
        sku = null,
        callbackUrl = null,
        transactionType = "TOPUP",
        entityOemId = null,
        entityDomain = null,
        entityPromoCode = null,
        userWallet = null,
        referrerUrl = null,
        guestWalletId = null,
        externalBuyerReference = null,
        isFreeTrial = null
      )
    )
      .thenReturn(Single.just(expectedModel))

    interactor.makeTopUpPayment(
      adyenPaymentMethod = payment,
      shouldStoreMethod = false,
      hasCvc = false,
      supportedShopperInteraction = emptyList(),
      returnUrl = "",
      value = TEST_FIAT_VALUE,
      currency = TEST_FIAT_CURRENCY,
      paymentType = "",
      transactionType = "TOPUP",
      packageName = "wallet"
    )
      .subscribe(testObserver)

    testObserver.assertNoErrors()
      .assertValue { it == expectedModel }
  }

  @Test
  fun submitRedirectTest() {
    val testObserver = TestObserver<PaymentModel>()
    val json = JsonObject()
    val expectedModel =
      PaymentModel(
        resultCode = null,
        refusalReason = null,
        refusalCode = null,
        action = null,
        redirectUrl = null,
        paymentData = "",
        uid = "uid",
        purchaseUid = null,
        hash = null,
        orderReference = null,
        fraudResultIds = emptyList(),
        status = PaymentModel.Status.COMPLETED,
        errorMessage = null,
        errorCode = null
      )
    Mockito.`when`(walletService.getWalletAddress())
      .thenReturn(Single.just(TEST_WALLET_ADDRESS))
    Mockito.`when`(
      repository.submitRedirect(
        uid = "uid",
        walletAddress = TEST_WALLET_ADDRESS,
        details = json,
        paymentData = null
      )
    )
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
      amount = BigDecimal(2),
      currency = TEST_FIAT_CURRENCY
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
      inAppPurchaseInteractor.getCompletedPurchaseBundle(
        /* type = */ "INAPP",
        /* merchantName = */ "merchant",
        /* sku = */ "sku",
        /* purchaseUid = */ null,
        /* orderReference = */ null,
        /* hash = */ null,
        /* scheduler = */ fakeSchedulers.main
      )
    )
      .thenReturn(Single.just(expectedModel))
    interactor.getCompletePurchaseBundle(
      type = "INAPP",
      merchantName = "merchant",
      sku = "sku",
      purchaseUid = null,
      orderReference = null,
      hash = null,
      scheduler = fakeSchedulers.main
    )
      .subscribe(testObserver)

    testObserver.assertNoErrors()
      .assertValue { it == expectedModel }
  }

  @Test
  fun convertToLocalFiatTest() {
    val testObserver = TestObserver<FiatValue>()
    val expectedFiatValue = FiatValue(
      amount = BigDecimal(2),
      currency = TEST_FIAT_CURRENCY
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
      PaymentModel(
        resultCode = null,
        refusalReason = null,
        refusalCode = null,
        action = null,
        redirectUrl = null,
        paymentData = null,
        uid = "uid",
        purchaseUid = null,
        hash = null,
        orderReference = null,
        fraudResultIds = emptyList(),
        status = PaymentModel.Status.COMPLETED,
        errorMessage = null,
        errorCode = null
      )
    Mockito.`when`(walletService.getAndSignCurrentWalletAddress())
      .thenReturn(Single.just(WalletAddressModel(
        address = TEST_WALLET_ADDRESS,
        signedAddress = TEST_WALLET_SIGNATURE
      )))
    Mockito.`when`(repository.getTransaction(
      uid = "uid",
      walletAddress = TEST_WALLET_ADDRESS
    ))
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
      PaymentModel(
        resultCode = null,
        refusalReason = null,
        refusalCode = null,
        action = null,
        redirectUrl = null,
        paymentData = null,
        uid = "uid",
        purchaseUid = null,
        hash = null,
        orderReference = null,
        fraudResultIds = emptyList(),
        status = PaymentModel.Status.COMPLETED,
        errorMessage = null,
        errorCode = 20
      )
    Mockito.`when`(walletService.getAndSignCurrentWalletAddress())
      .thenReturn(Single.just(WalletAddressModel(
        address = TEST_WALLET_ADDRESS,
        signedAddress = TEST_WALLET_SIGNATURE
      )))
    Mockito.`when`(repository.getTransaction(
      uid = "uid",
      walletAddress = TEST_WALLET_ADDRESS
    ))
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
      PaymentModel(
        resultCode = null,
        refusalReason = null,
        refusalCode = null,
        action = null,
        redirectUrl = null,
        paymentData = null,
        uid = "uid",
        purchaseUid = null,
        hash = null,
        orderReference = null,
        fraudResultIds = emptyList(),
        status = PaymentModel.Status.COMPLETED,
        errorMessage = null,
        errorCode = 20
      )
    val expectedFailModel =
      PaymentModel(
        resultCode = null,
        refusalReason = null,
        refusalCode = null,
        action = null,
        redirectUrl = null,
        paymentData = null,
        uid = "uid",
        purchaseUid = null,
        hash = null,
        orderReference = null,
        fraudResultIds = emptyList(),
        status = PaymentModel.Status.COMPLETED,
        errorMessage = null,
        errorCode = null
      )
    Mockito.`when`(walletService.getAndSignCurrentWalletAddress())
      .thenReturn(Single.just(WalletAddressModel(
        address = TEST_WALLET_ADDRESS,
        signedAddress = TEST_WALLET_SIGNATURE
      )))
    Mockito.`when`(repository.getTransaction(
      uid = "uid",
      walletAddress = TEST_WALLET_ADDRESS
    ))
      .thenReturn(Single.just(expectedFailModel), Single.just(expectedSuccessModel))
    interactor.getFailedTransactionReason("uid")
      .subscribe(testObserver)
    fakeSchedulers.testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)
    fakeSchedulers.testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)
    testObserver.assertNoErrors()
      .assertValue { it == expectedSuccessModel }
  }
}

package com.appcoins.wallet.bdsbilling

import com.appcoins.wallet.bdsbilling.repository.*
import com.appcoins.wallet.bdsbilling.repository.entity.Gateway
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal

@RunWith(MockitoJUnitRunner::class)
class BillingPaymentProofSubmissionTest {
  companion object {
    const val walletAddress = "wallet_address"
    const val developerAddress = "developer_address"
    const val oemAddress = "developer_address"
    const val storeAddress = "store_address"
    const val signedContent = "signed $walletAddress"
    const val productName = "product_name"
    const val packageName = "package_name"
    const val paymentId = "payment_id"
    const val paymentToken = "paymentToken"
    const val paymentType = "type"
    const val developerPayload = "developer_payload"
    const val origin = "origin"
    const val priceValue = "1"
    const val currency = "APPC"
    const val type = "APPC"
    const val callback = "callback_url"
    const val orderReference = "order_reference"
    const val referrerUrl = "a_random_url"
  }

  @Mock
  lateinit var brokerBdsApi: RemoteRepository.BrokerBdsApi

  @Mock
  lateinit var inappBdsApi: InappBillingApi

  @Mock
  lateinit var subscriptionBillingApi: SubscriptionBillingApi
  lateinit var billing: BillingPaymentProofSubmission
  lateinit var scheduler: TestScheduler

  @Before
  fun setUp() {
    scheduler = TestScheduler()

    billing = BillingPaymentProofSubmissionImpl.Builder()
      .setBrokerBdsApi(brokerBdsApi)
      .setInappApi(inappBdsApi)
      .setScheduler(scheduler)
      .setWalletService(object : WalletService {
        override fun getWalletAddress(): Single<String> = Single.just(walletAddress)
        override fun signContent(content: String): Single<String> = Single.just(signedContent)
        override fun signSpecificWalletAddressContent(
          walletAddress: String,
          content: String
        ): Single<String> = Single.just(signedContent)

        override fun getAndSignCurrentWalletAddress(): Single<WalletAddressModel> =
          Single.just(WalletAddressModel(walletAddress, signedContent))

        override fun getAndSignSpecificWalletAddress(walletAddress: String): Single<WalletAddressModel> =
          Single.just(WalletAddressModel(walletAddress, signedContent))

        override fun getWalletOrCreate(): Single<String> = Single.just(walletAddress)
        override fun findWalletOrCreate(): Observable<String> = Observable.just(walletAddress)
      })
      .setBdsApiSecondary(object : BdsApiSecondary {
        override fun getWallet(packageName: String): Single<GetWalletResponse> {
          return Single.just(GetWalletResponse(Data("developer_address")))
        }
      })
      .setSubscriptionBillingService(subscriptionBillingApi)
      .build()

    `when`(
      brokerBdsApi.createTransaction(
        paymentType,
        origin,
        packageName,
        priceValue,
        currency,
        productName,
        type,
        null,
        developerAddress,
        storeAddress,
        oemAddress,
        null,
        paymentId,
        developerPayload,
        callback,
        orderReference,
        referrerUrl,
        walletAddress,
        signedContent
      )
    ).thenReturn(
      Single.just(
        Transaction(
          paymentId,
          Transaction.Status.FAILED,
          Gateway(Gateway.Name.appcoins_credits, "APPC C", "icon"),
          null,
          null,
          "orderReference",
          null,
          "",
          null,
          ""
        )
      )
    )

    `when`(
      brokerBdsApi.patchTransaction(
        paymentType,
        paymentId,
        walletAddress,
        signedContent,
        paymentToken
      )
    ).thenReturn(Completable.complete())
  }

  @Test
  fun start() {
    val authorizationDisposable = TestObserver<Any>()
    val purchaseDisposable = TestObserver<Any>()
    billing.processAuthorizationProof(
      AuthorizationProof(
        paymentType,
        paymentId,
        productName,
        packageName,
        BigDecimal.ONE,
        storeAddress,
        oemAddress,
        developerAddress,
        type,
        origin,
        developerPayload,
        callback,
        orderReference,
        referrerUrl
      )
    ).subscribe(authorizationDisposable)
    scheduler.triggerActions()

    billing.processPurchaseProof(
      PaymentProof(
        paymentType,
        paymentId,
        paymentToken,
        productName,
        packageName,
        storeAddress,
        oemAddress
      )
    ).subscribe(purchaseDisposable)
    scheduler.triggerActions()


    authorizationDisposable.assertNoErrors().assertComplete()
    purchaseDisposable.assertNoErrors().assertComplete()
    verify(brokerBdsApi, times(1)).createTransaction(
      paymentType,
      origin,
      packageName,
      priceValue,
      currency,
      productName,
      type,
      null,
      developerAddress,
      storeAddress,
      oemAddress,
      null,
      paymentId,
      developerPayload,
      callback,
      orderReference,
      referrerUrl,
      walletAddress,
      signedContent
    )
    verify(brokerBdsApi, times(1)).patchTransaction(
      paymentType,
      paymentId,
      walletAddress,
      signedContent,
      paymentToken
    )
  }
}
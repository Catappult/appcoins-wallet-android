package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.repository.RegisterAuthorizationBody
import com.appcoins.wallet.billing.repository.RegisterAuthorizationResponse
import com.appcoins.wallet.billing.repository.RegisterPaymentBody
import com.appcoins.wallet.billing.repository.RemoteRepository
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BillingPaymentProofSubmissionTest {
  companion object {
    val walletAddress = "wallet_address"
    val developerAddress = "developer_address"
    val oemAddress = "developer_address"
    val storeAddress = "store_address"
    val signedContent = "signed $walletAddress"
    val productName = "product_name"
    val packageName = "package_name"
    val paymentId = "payment_id"
    val paymentToken = "paymentToken"
    val paymentType = "type"
  }

  @Mock
  lateinit var api: RemoteRepository.BdsApi
  lateinit var billing: BillingPaymentProofSubmission
  lateinit var scheduler: TestScheduler
  @Before
  fun setUp() {
    scheduler = TestScheduler()

    billing = BillingPaymentProofSubmission.Builder().setApi(api).setScheduler(scheduler)
        .setWalletService(object : WalletService {
          override fun getWalletAddress(): Single<String> = Single.just(walletAddress)
          override fun signContent(content: String): Single<String> = Single.just(signedContent)
        }).build()

    `when`(api.registerAuthorization(paymentType, walletAddress, signedContent,
        RegisterAuthorizationBody(productName, packageName, paymentId, developerAddress,
            storeAddress))).thenReturn(
        Single.just(RegisterAuthorizationResponse(paymentId, paymentType, "status", "data")))

    `when`(api.registerPayment(paymentType, paymentId, walletAddress, signedContent,
        RegisterPaymentBody(paymentToken))).thenReturn(Completable.complete())
  }

  @Test
  fun start() {
    billing.start()
    val authorizationPublisher: PublishSubject<AuthorizationProof> = PublishSubject.create()
    val paymentPublisher: PublishSubject<PaymentProof> = PublishSubject.create()
    billing.addAuthorizationProofSource(authorizationPublisher)
    billing.addPaymentProofSource(paymentPublisher)
    scheduler.triggerActions()
    authorizationPublisher.onNext(
        AuthorizationProof(paymentType, paymentId, productName, packageName, storeAddress,
            oemAddress, developerAddress))
    scheduler.triggerActions()

    paymentPublisher.onNext(PaymentProof(paymentType, paymentId, paymentToken, productName,
        packageName, storeAddress, oemAddress))
    scheduler.triggerActions()


    verify(api, times(1)).registerAuthorization(paymentType, walletAddress, signedContent,
        RegisterAuthorizationBody(productName, packageName, paymentId, developerAddress,
            storeAddress))
    verify(api, times(1)).registerPayment(paymentType, paymentId, walletAddress, signedContent,
        RegisterPaymentBody(paymentToken))

  }
}
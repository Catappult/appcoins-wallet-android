package com.appcoins.wallet.bdsbilling

import com.appcoins.wallet.bdsbilling.repository.BdsApiSecondary
import com.appcoins.wallet.bdsbilling.repository.Data
import com.appcoins.wallet.bdsbilling.repository.GetWalletResponse
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.bdsbilling.repository.entity.Gateway
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import io.reactivex.Completable
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
    val developerPayload = "developer_payload"
    val origin = "origin"
    val priceValue = "1"
    val currency = "APPC"
    val type = "APPC"
    val callback = "callback_url"
    val orderReference = "order_reference"
  }

  @Mock
  lateinit var api: RemoteRepository.BdsApi
  lateinit var billing: BillingPaymentProofSubmission
  lateinit var scheduler: TestScheduler
  @Before
  fun setUp() {
    scheduler = TestScheduler()

    billing = BillingPaymentProofSubmissionImpl.Builder().setApi(api).setScheduler(scheduler)
        .setWalletService(object : WalletService {
          override fun getWalletAddress(): Single<String> = Single.just(walletAddress)
          override fun signContent(content: String): Single<String> = Single.just(signedContent)
        }).setBdsApiSecondary(object : BdsApiSecondary {
          override fun getWallet(packageName: String): Single<GetWalletResponse> {
            return Single.just(GetWalletResponse(Data("developer_address")))
          }
        }).build()

    `when`(
        api.createTransaction(paymentType, origin, packageName, priceValue, currency, productName,
            type, null, developerAddress, storeAddress, oemAddress, paymentId,
            developerPayload, callback, orderReference,
            walletAddress,
            signedContent)).thenReturn(
        Single.just(Transaction(paymentId, Transaction.Status.FAILED,
            Gateway(Gateway.Name.appcoins_credits, "APPC C", "icon"), null, "orderReference",
            null)))

    `when`(api.patchTransaction(paymentType, paymentId, walletAddress, signedContent,
        paymentToken)).thenReturn(Completable.complete())
  }

  @Test
  fun start() {
    val authorizationDisposable = TestObserver<Any>()
    val purchaseDisposable = TestObserver<Any>()
    billing.processAuthorizationProof(
        AuthorizationProof(paymentType, paymentId, productName, packageName, BigDecimal.ONE,
            storeAddress,
            oemAddress, developerAddress, type, origin, developerPayload, callback, orderReference))
        .subscribe(authorizationDisposable)
    scheduler.triggerActions()

    billing.processPurchaseProof(PaymentProof(paymentType, paymentId, paymentToken, productName,
        packageName, storeAddress, oemAddress)).subscribe(purchaseDisposable)
    scheduler.triggerActions()


    authorizationDisposable.assertNoErrors().assertComplete()
    purchaseDisposable.assertNoErrors().assertComplete()
    verify(api, times(1)).createTransaction(paymentType, origin, packageName, priceValue, currency,
        productName, type, null, developerAddress, storeAddress, oemAddress, paymentId,
        developerPayload, callback, orderReference, walletAddress, signedContent)
    verify(api, times(1)).patchTransaction(paymentType, paymentId, walletAddress, signedContent,
        paymentToken)

  }
}
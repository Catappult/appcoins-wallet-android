package com.appcoins.wallet.bdsbilling

import com.appcoins.wallet.bdsbilling.repository.BdsApiResponseMapper
import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.bdsbilling.repository.InAppMapper
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.bdsbilling.repository.SubscriptionsMapper
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.network.microservices.api.product.InappBillingApi
import com.appcoins.wallet.core.network.microservices.api.product.SubscriptionBillingApi
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.walletservices.WalletService
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap

class BillingPaymentProofSubmissionImpl
internal constructor(
    private val walletService: WalletService,
    private val repository: BillingRepository,
    private val networkScheduler: Scheduler,
    private val transactionFromApprove: MutableMap<String, Transaction>,
    private val transactionIdsFromBuy: MutableMap<String, String>,
    private val ewtObtainer: EwtAuthenticatorService,
    private val rxSchedulers: RxSchedulers,
) : BillingPaymentProofSubmission {

  override fun processPurchaseProof(paymentProof: PaymentProof): Single<Transaction> =
      transactionFromApprove[paymentProof.approveProof]?.let { transaction ->
        registerPaymentProof(transaction.uid, paymentProof.paymentProof, paymentProof.paymentType)
            .andThen(Single.just(transaction))
      }
          ?: Single.error(
              IllegalArgumentException("No payment id for {${paymentProof.approveProof}}"))

  override fun processAuthorizationProof(
      authorizationProof: AuthorizationProof
  ): Single<Transaction> =
      registerAuthorizationProof(
              authorizationProof.id,
              authorizationProof.paymentType,
              authorizationProof.productName,
              authorizationProof.packageName,
              authorizationProof.priceValue,
              authorizationProof.developerAddress,
              authorizationProof.entityOemId,
              authorizationProof.entityDomain,
              authorizationProof.origin,
              authorizationProof.type,
              authorizationProof.developerPayload,
              authorizationProof.callback,
              authorizationProof.orderReference,
              authorizationProof.referrerUrl)
          .doOnSuccess { transaction ->
            transactionFromApprove[authorizationProof.id] = transaction
          }

  override fun registerPaymentProof(
      paymentId: String,
      paymentProof: String,
      paymentType: String
  ): Completable =
      walletService
          .getWalletAddress()
          .observeOn(networkScheduler)
          .flatMapCompletable { walletAddress ->
            walletService
                .signContent(walletAddress)
                .observeOn(networkScheduler)
                .flatMapCompletable { signedData ->
                  repository.registerPaymentProof(
                      paymentId, paymentType, walletAddress, signedData, paymentProof)
                }
          }
          .andThen(Completable.fromAction { transactionIdsFromBuy[paymentProof] = paymentId })

  override fun registerAuthorizationProof(
      id: String,
      paymentType: String,
      productName: String?,
      packageName: String,
      priceValue: BigDecimal,
      developerWallet: String,
      entityOemId: String?,
      entityDomain: String?,
      origin: String,
      type: String,
      developerPayload: String?,
      callback: String?,
      orderReference: String?,
      referrerUrl: String?
  ): Single<Transaction> =
      walletService.getWalletAddress().observeOn(networkScheduler).flatMap { walletAddress ->
        walletService.signContent(walletAddress).observeOn(networkScheduler).flatMap { signedData ->
          repository.registerAuthorizationProof(
              id,
              paymentType,
              walletAddress,
              signedData,
              productName,
              packageName,
              priceValue,
              entityOemId,
              entityDomain,
              origin,
              type,
              developerPayload,
              callback,
              orderReference,
              referrerUrl)
        }
      }

  override fun saveTransactionId(transaction: Transaction) {
    transactionFromApprove[transaction.uid] = transaction
  }

  override fun getTransactionFromUid(uid: String): Transaction? = transactionFromApprove[uid]

  override fun getTransactionId(buyHash: String): String? = transactionIdsFromBuy[buyHash]

  companion object {
    inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
  }

  class Builder {
    private var walletService: WalletService? = null
    private var networkScheduler: Scheduler = Schedulers.io()
    private var brokerBdsApi: BrokerBdsApi? = null
    private var inappApi: InappBillingApi? = null
    private var subscriptionBillingApi: SubscriptionBillingApi? = null
    private var ewtObtainer: EwtAuthenticatorService? = null
    private var rxSchedulers: RxSchedulers? = null

    fun setBrokerBdsApi(brokerBdsApi: BrokerBdsApi) = apply { this.brokerBdsApi = brokerBdsApi }

    fun setInappApi(inappApi: InappBillingApi) = apply { this.inappApi = inappApi }

    fun setSubscriptionBillingService(subscriptionBillingApi: SubscriptionBillingApi) = apply {
      this.subscriptionBillingApi = subscriptionBillingApi
    }

    fun setScheduler(scheduler: Scheduler) = apply { this.networkScheduler = scheduler }

    fun setWalletService(walletService: WalletService) = apply {
      this.walletService = walletService
    }

    fun setEwtObtainer(ewtObtainer: EwtAuthenticatorService) = apply {
      this.ewtObtainer = ewtObtainer
    }

    fun setRxSchedulers(rxSchedulers: RxSchedulers) = apply { this.rxSchedulers = rxSchedulers }

    fun build(): BillingPaymentProofSubmissionImpl =
        ewtObtainer?.let { ewtObtainer ->
          rxSchedulers?.let { rxSchedulers ->
            walletService?.let { walletService ->
              brokerBdsApi?.let { brokerBdsApi ->
                inappApi?.let { inappApi ->
                  subscriptionBillingApi?.let { subscriptionsApi ->
                    BillingPaymentProofSubmissionImpl(
                        walletService,
                        BdsRepository(
                            RemoteRepository(
                                brokerBdsApi,
                                inappApi,
                                BdsApiResponseMapper(SubscriptionsMapper(), InAppMapper()),
                                subscriptionsApi,
                                ewtObtainer,
                                rxSchedulers)),
                        networkScheduler,
                        ConcurrentHashMap(),
                        ConcurrentHashMap(),
                        ewtObtainer,
                        rxSchedulers)
                  } ?: throw IllegalArgumentException("SubscriptionBillingService not defined")
                } ?: throw IllegalArgumentException("InappBdsApi not defined")
              } ?: throw IllegalArgumentException("BrokerBdsApi not defined")
            } ?: throw IllegalArgumentException("WalletService not defined")
          } ?: throw IllegalArgumentException("ewtObtainer not defined")
        } ?: throw IllegalArgumentException("rxSchedulers not defined")
  }
}

data class AuthorizationProof(
    val paymentType: String,
    val id: String,
    val productName: String?,
    val packageName: String,
    val priceValue: BigDecimal,
    val entityOemId: String?,
    val entityDomain: String?,
    val developerAddress: String,
    val type: String,
    val origin: String,
    val developerPayload: String?,
    val callback: String?,
    val orderReference: String?,
    val referrerUrl: String?
)

data class PaymentProof(
    val paymentType: String,
    val approveProof: String,
    val paymentProof: String,
    val productName: String?,
    val packageName: String,
    val entityOemId: String?,
    val entityDomain: String?
)

package com.appcoins.wallet.bdsbilling

import com.appcoins.wallet.bdsbilling.mappers.ExternalBillingSerializer
import com.appcoins.wallet.bdsbilling.repository.*
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.bdsbilling.subscriptions.SubscriptionBillingApi
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap

class BillingPaymentProofSubmissionImpl internal constructor(
  private val walletService: WalletService,
  private val repository: BillingRepository,
  private val networkScheduler: Scheduler,
  private val transactionFromApprove: MutableMap<String, Transaction>,
  private val transactionIdsFromBuy: MutableMap<String, String>
) : BillingPaymentProofSubmission {

  override fun processPurchaseProof(paymentProof: PaymentProof): Single<Transaction> {
    return transactionFromApprove[paymentProof.approveProof]?.let { transaction ->
      registerPaymentProof(transaction.uid, paymentProof.paymentProof, paymentProof.paymentType)
        .andThen(Single.just(transaction))
    } ?: Single.error(
      IllegalArgumentException("No payment id for {${paymentProof.approveProof}}")
    )
  }

  override fun processAuthorizationProof(
    authorizationProof: AuthorizationProof
  ): Single<Transaction> {
    return registerAuthorizationProof(
      authorizationProof.id, authorizationProof.paymentType,
      authorizationProof.productName, authorizationProof.packageName,
      authorizationProof.priceValue, authorizationProof.developerAddress,
      authorizationProof.entityOemId, authorizationProof.entityDomain, authorizationProof.origin,
      authorizationProof.type,
      authorizationProof.developerPayload,
      authorizationProof.callback, authorizationProof.orderReference,
      authorizationProof.referrerUrl
    )
      .doOnSuccess { transaction ->
        transactionFromApprove[authorizationProof.id] = transaction
      }
  }

  override fun registerPaymentProof(
    paymentId: String, paymentProof: String,
    paymentType: String
  ): Completable {
    return walletService.getWalletAddress()
      .observeOn(networkScheduler)
      .flatMapCompletable { walletAddress ->
        walletService.signContent(walletAddress)
          .observeOn(networkScheduler)
          .flatMapCompletable { signedData ->
            repository.registerPaymentProof(
              paymentId, paymentType, walletAddress, signedData,
              paymentProof
            )
          }
      }
      .andThen(Completable.fromAction { transactionIdsFromBuy[paymentProof] = paymentId })
  }

  override fun registerAuthorizationProof(
    id: String, paymentType: String,
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
  ): Single<Transaction> {
    return walletService.getWalletAddress()
      .observeOn(networkScheduler)
      .flatMap { walletAddress ->
        walletService.signContent(walletAddress)
          .observeOn(networkScheduler)
          .flatMap { signedData ->
            repository.registerAuthorizationProof(
              id, paymentType, walletAddress, signedData,
              productName, packageName, priceValue, developerWallet, entityOemId,
              entityDomain,
              origin, type, developerPayload, callback, orderReference, referrerUrl
            )
          }
      }
  }

  override fun saveTransactionId(transaction: Transaction) {
    transactionFromApprove[transaction.uid] = transaction
  }

  override fun getTransactionFromUid(uid: String): Transaction? {
    return transactionFromApprove[uid]
  }

  override fun getTransactionId(buyHash: String): String? {
    return transactionIdsFromBuy[buyHash]
  }

  companion object {
    inline fun build(block: Builder.() -> Unit) =
      Builder().apply(block)
        .build()
  }

  class Builder {
    private var walletService: WalletService? = null
    private var networkScheduler: Scheduler = Schedulers.io()
    private var brokerBdsApi: RemoteRepository.BrokerBdsApi? = null
    private var inappBdsApi: RemoteRepository.InappBdsApi? = null
    private var bdsApiSecondary: BdsApiSecondary? = null
    private var subscriptionApi: SubscriptionBillingApi? = null
    private var billingSerializer: ExternalBillingSerializer? = null

    fun setBrokerBdsApi(brokerBdsApi: RemoteRepository.BrokerBdsApi) =
      apply { this.brokerBdsApi = brokerBdsApi }

    fun setInappBdsApi(inappBdsApi: RemoteRepository.InappBdsApi) =
      apply { this.inappBdsApi = inappBdsApi }

    fun setBillingSerializer(billingSerializer: ExternalBillingSerializer) =
      apply { this.billingSerializer = billingSerializer }

    fun setBdsApiSecondary(bdsApi: BdsApiSecondary) = apply { bdsApiSecondary = bdsApi }

    fun setSubscriptionBillingService(subscriptionBillingApi: SubscriptionBillingApi) =
      apply { subscriptionApi = subscriptionBillingApi }

    fun setScheduler(scheduler: Scheduler) = apply { this.networkScheduler = scheduler }

    fun setWalletService(walletService: WalletService) =
      apply { this.walletService = walletService }

    fun build(): BillingPaymentProofSubmissionImpl {
      return walletService?.let { walletService ->
        brokerBdsApi?.let { brokerBdsApi ->
          inappBdsApi?.let { inappBdsApi ->
            bdsApiSecondary?.let { bdsApiSecondary ->
              subscriptionApi?.let { subscriptionApi ->
                billingSerializer?.let { billingSerializer ->
                  BillingPaymentProofSubmissionImpl(
                    walletService, BdsRepository(
                      RemoteRepository(
                        brokerBdsApi, inappBdsApi, BdsApiResponseMapper(
                          SubscriptionsMapper(), InAppMapper(
                            ExternalBillingSerializer()
                          )
                        ), bdsApiSecondary, subscriptionApi, billingSerializer
                      )
                    ),
                    networkScheduler, ConcurrentHashMap(), ConcurrentHashMap()
                  )
                } ?: throw IllegalArgumentException("BillingSerializer not defined")
              } ?: throw IllegalArgumentException("SubscriptionBillingService not defined")
            } ?: throw IllegalArgumentException("BdsApiSecondary not defined")
          } ?: throw IllegalArgumentException("InappBdsApi not defined")
        } ?: throw IllegalArgumentException("BrokerBdsApi not defined")
      } ?: throw IllegalArgumentException("WalletService not defined")
    }
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
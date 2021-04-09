package com.appcoins.wallet.bdsbilling

import com.appcoins.wallet.bdsbilling.mappers.ExternalBillingSerializer
import com.appcoins.wallet.bdsbilling.repository.*
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
    private val transactionIdsFromApprove: MutableMap<String, String>,
    private val transactionIdsFromBuy: MutableMap<String, String>) : BillingPaymentProofSubmission {

  override fun processPurchaseProof(paymentProof: PaymentProof): Completable {
    return transactionIdsFromApprove[paymentProof.approveProof]?.let { paymentId ->
      registerPaymentProof(paymentId, paymentProof.paymentProof, paymentProof.paymentType)
    } ?: Completable.error(
        IllegalArgumentException("No payment id for {${paymentProof.approveProof}}"))
  }

  override fun processAuthorizationProof(authorizationProof: AuthorizationProof): Completable {
    return registerAuthorizationProof(authorizationProof.id, authorizationProof.paymentType,
        authorizationProof.productName, authorizationProof.packageName,
        authorizationProof.priceValue, authorizationProof.developerAddress,
        authorizationProof.storeAddress, authorizationProof.origin, authorizationProof.type,
        authorizationProof.oemAddress, authorizationProof.developerPayload,
        authorizationProof.callback, authorizationProof.orderReference,
        authorizationProof.referrerUrl)
        .doOnSuccess { paymentId -> transactionIdsFromApprove[authorizationProof.id] = paymentId }
        .ignoreElement()
  }

  override fun registerPaymentProof(paymentId: String, paymentProof: String,
                                    paymentType: String): Completable {
    return walletService.getWalletAddress()
        .observeOn(networkScheduler)
        .flatMapCompletable { walletAddress ->
          walletService.signContent(walletAddress)
              .observeOn(networkScheduler)
              .flatMapCompletable { signedData ->
                repository.registerPaymentProof(paymentId, paymentType, walletAddress, signedData,
                    paymentProof)
              }
        }
        .andThen(Completable.fromAction { transactionIdsFromBuy[paymentProof] = paymentId })
  }

  override fun registerAuthorizationProof(id: String, paymentType: String, productName: String?,
                                          packageName: String, priceValue: BigDecimal,
                                          developerWallet: String, storeWallet: String,
                                          origin: String, type: String, oemWallet: String,
                                          developerPayload: String?, callback: String?,
                                          orderReference: String?,
                                          referrerUrl: String?): Single<String> {
    return walletService.getWalletAddress()
        .observeOn(networkScheduler)
        .flatMap { walletAddress ->
          walletService.signContent(walletAddress)
              .observeOn(networkScheduler)
              .flatMap { signedData ->
                repository.registerAuthorizationProof(id, paymentType, walletAddress, signedData,
                    productName, packageName, priceValue, developerWallet, storeWallet, origin,
                    type, oemWallet, developerPayload, callback, orderReference, referrerUrl)
              }
        }
  }

  override fun saveTransactionId(key: String) {
    transactionIdsFromApprove[key] = key
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
    private var api: BdsApi? = null
    private var bdsApiSecondary: BdsApiSecondary? = null
    private var subscriptionApi: SubscriptionBillingApi? = null

    fun setApi(bdsApi: BdsApi) = apply { api = bdsApi }

    fun setBdsApiSecondary(bdsApi: BdsApiSecondary) = apply { bdsApiSecondary = bdsApi }

    fun setSubscriptionBillingService(subscriptionBillingApi: SubscriptionBillingApi) =
        apply { subscriptionApi = subscriptionBillingApi }

    fun setScheduler(scheduler: Scheduler) = apply { this.networkScheduler = scheduler }

    fun setWalletService(walletService: WalletService) =
        apply { this.walletService = walletService }

    fun build(): BillingPaymentProofSubmissionImpl {
      return walletService?.let { walletService ->
        api?.let { api ->
          bdsApiSecondary?.let { bdsApiSecondary ->
            subscriptionApi?.let { subscriptionApi ->
              BillingPaymentProofSubmissionImpl(walletService, BdsRepository(
                  RemoteRepository(api, BdsApiResponseMapper(SubscriptionsMapper(), InAppMapper(
                      ExternalBillingSerializer())), bdsApiSecondary, subscriptionApi)),
                  networkScheduler, ConcurrentHashMap(), ConcurrentHashMap())
            } ?: throw IllegalArgumentException("SubscriptionBillingService not defined")
          } ?: throw IllegalArgumentException("BdsApiSecondary not defined")
        } ?: throw IllegalArgumentException("BdsApi not defined")
      } ?: throw IllegalArgumentException("WalletService not defined")
    }
  }

}

data class AuthorizationProof(val paymentType: String,
                              val id: String,
                              val productName: String?,
                              val packageName: String,
                              val priceValue: BigDecimal,
                              val storeAddress: String,
                              val oemAddress: String,
                              val developerAddress: String,
                              val type: String,
                              val origin: String,
                              val developerPayload: String?,
                              val callback: String?,
                              val orderReference: String?,
                              val referrerUrl: String?)

data class PaymentProof(val paymentType: String,
                        val approveProof: String,
                        val paymentProof: String,
                        val productName: String?,
                        val packageName: String,
                        val storeAddress: String,
                        val oemAddress: String)
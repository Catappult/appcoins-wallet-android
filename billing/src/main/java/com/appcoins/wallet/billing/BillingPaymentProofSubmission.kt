package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.repository.BdsApiResponseMapper
import com.appcoins.wallet.billing.repository.BdsRepository
import com.appcoins.wallet.billing.repository.RemoteRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.ConcurrentHashMap

class BillingPaymentProofSubmission internal constructor(
    private val authorizationEventMerger: EventMerger<AuthorizationProof>,
    private val paymentEventMerger: EventMerger<PaymentProof>,
    private val walletService: WalletService,
    private val repository: Repository,
    private val networkScheduler: Scheduler,
    private val paymentIds: MutableMap<String, String>,
    private val disposables: CompositeDisposable) {

  fun start() {
    disposables.add(authorizationEventMerger.getEvents()
        .subscribeOn(networkScheduler)
        .flatMapCompletable { processAuthorizationProof(it) }
        .doOnError { it.printStackTrace() }
        .retry()
        .subscribe())

    disposables.add(paymentEventMerger.getEvents()
        .subscribeOn(networkScheduler)
        .flatMapCompletable {
          processPurchaseProof(it)
        }
        .doOnError { it.printStackTrace() }
        .retry()
        .subscribe())
  }

  fun processPurchaseProof(paymentProof: PaymentProof): Completable {
    return paymentIds[paymentProof.approveProof]?.let { paymentId ->
      registerPaymentProof(paymentId, paymentProof.paymentProof, paymentProof.paymentType)
    } ?: Completable.error(
        IllegalArgumentException("No payment id for {${paymentProof.approveProof}}"))
  }

  fun processAuthorizationProof(authorizationProof: AuthorizationProof): Completable {
    return registerAuthorizationProof(authorizationProof.id, authorizationProof.paymentType,
        authorizationProof.productName, authorizationProof.packageName,
        authorizationProof.developerAddress, authorizationProof.storeAddress)
        .doOnSuccess { paymentId -> paymentIds[authorizationProof.id] = paymentId }.toCompletable()
  }

  private fun registerPaymentProof(paymentId: String, paymentProof: String,
                                   paymentType: String): Completable {
    return walletService.getWalletAddress().observeOn(networkScheduler)
        .flatMapCompletable { walletAddress ->
          walletService.signContent(walletAddress).observeOn(networkScheduler)
              .flatMapCompletable { signedData ->
                repository.registerPaymentProof(paymentId, paymentType, walletAddress, signedData,
                    paymentProof)
              }
        }
  }

  private fun registerAuthorizationProof(id: String, paymentType: String, productName: String,
                                         packageName: String,
                                         developerWallet: String,
                                         storeWallet: String): Single<String> {
    return walletService.getWalletAddress().observeOn(networkScheduler).flatMap { walletAddress ->
      walletService.signContent(walletAddress).observeOn(networkScheduler).flatMap { signedData ->
        repository.registerAuthorizationProof(id, paymentType, walletAddress, signedData,
            productName,
            packageName, developerWallet, storeWallet)

      }
    }
  }

  fun stop() {
    disposables.clear()
    authorizationEventMerger.stop()
    paymentEventMerger.stop()
  }

  fun addAuthorizationProofSource(source: Observable<AuthorizationProof>) {
    authorizationEventMerger.addSource(source)
  }

  fun addPaymentProofSource(source: Observable<PaymentProof>) {
    paymentEventMerger.addSource(source)
  }

  companion object {
    inline fun build(block: BillingPaymentProofSubmission.Builder.() -> Unit) =
        BillingPaymentProofSubmission.Builder().apply(block).build()
  }

  class Builder {
    private var walletService: WalletService? = null
    private var networkScheduler: Scheduler = Schedulers.io()
    private var api: RemoteRepository.BdsApi? = null

    fun setApi(bdsApi: RemoteRepository.BdsApi) = apply { api = bdsApi }

    fun setScheduler(scheduler: Scheduler) = apply { this.networkScheduler = scheduler }

    fun setWalletService(walletService: WalletService) =
        apply { this.walletService = walletService }

    fun build(): BillingPaymentProofSubmission {
      return walletService?.let { walletService ->
        api?.let { api ->
          BillingPaymentProofSubmission(
              EventMerger(PublishSubject.create(), CompositeDisposable()),
              EventMerger(PublishSubject.create(), CompositeDisposable()),
              walletService, BdsRepository(
              RemoteRepository(api, BdsApiResponseMapper()),
              BillingThrowableCodeMapper()), networkScheduler, ConcurrentHashMap(),
              CompositeDisposable())
        } ?: throw IllegalArgumentException("BdsApi not defined")
      } ?: throw IllegalArgumentException("WalletService not defined")
    }
  }

}

data class AuthorizationProof(val paymentType: String,
                              val id: String,
                              val productName: String,
                              val packageName: String,
                              val storeAddress: String,
                              val oemAddress: String,
                              val developerAddress: String)

data class PaymentProof(val paymentType: String,
                        val approveProof: String,
                        val paymentProof: String,
                        val productName: String,
                        val packageName: String,
                        val storeAddress: String,
                        val oemAddress: String)
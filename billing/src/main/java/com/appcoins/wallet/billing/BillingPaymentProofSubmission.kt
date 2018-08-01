package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.repository.BdsApiResponseMapper
import com.appcoins.wallet.billing.repository.BdsRepository
import com.appcoins.wallet.billing.repository.RemoteRepository
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class BillingPaymentProofSubmission internal constructor(
    private val eventMerger: EventMerger<AuthorizationProof>,
    private val walletService: WalletService,
    private val repository: Repository, private val networkScheduler: Scheduler) {
  var disposable: Disposable? = null
  fun start() {
    disposable = eventMerger.getEvents()
        .subscribeOn(networkScheduler)
        .flatMapSingle {
          registerProof(it.id, it.paymentType, it.productName, it.packageName, it.oemAddress,
              it.storeAddress)
        }
        .doOnError { it.printStackTrace() }
        .retry()
        .subscribe()
  }

  private fun registerProof(id: String, paymentType: String, productName: String,
                            packageName: String,
                            developerWallet: String,
                            storeWallet: String): Single<String> {
    return walletService.getWalletAddress().observeOn(networkScheduler).flatMap { walletAddress ->
      walletService.signContent(walletAddress).observeOn(networkScheduler).flatMap { signedData ->
        repository.registerProof(id, paymentType, walletAddress, signedData, productName,
            packageName, developerWallet, storeWallet)

      }
    }
  }

  fun stop() {
    disposable?.let { if (!it.isDisposed) it.dispose() }
    eventMerger.stop()
  }

  fun addProofSource(source: Observable<AuthorizationProof>) {
    eventMerger.addSource(source)
  }

  companion object {
    inline fun build(billingDependenciesProvider: BillingDependenciesProvider,
                     block: BillingPaymentProofSubmission.Builder.() -> Unit) =
        BillingPaymentProofSubmission.Builder(billingDependenciesProvider).apply(block).build()
  }

  class Builder(private val billingDependenciesProvider: BillingDependenciesProvider) {
    var networkScheduler: Scheduler = Schedulers.io()
    fun build() =
        BillingPaymentProofSubmission(
            EventMerger(PublishSubject.create(), CompositeDisposable()),
            billingDependenciesProvider.getWalletService(),
            BdsRepository(
                RemoteRepository(billingDependenciesProvider.getBdsApi(), BdsApiResponseMapper()),
                BillingThrowableCodeMapper()), networkScheduler)
  }

}

data class AuthorizationProof(val paymentType: String,
                              val id: String,
                              val productName: String,
                              val packageName: String,
                              val storeAddress: String,
                              val oemAddress: String)

package com.asfoundation.wallet.wallet_validation.poa

import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.CreateWalletInteract
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.repository.SmsValidationRepositoryType
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class PoaWalletValidationPresenter(
    private val view: PoaWalletValidationView,
    private val smsValidationRepository: SmsValidationRepositoryType,
    private val walletInteractor: FindDefaultWalletInteract,
    private val createWalletInteractor: CreateWalletInteract,
    private val disposables: CompositeDisposable,
    private val viewScheduler: Scheduler,
    private val networkScheduler: Scheduler
) {

  fun present() {
    handleWalletValidation()
  }

  private fun handleWalletValidation() {
    disposables.add(walletInteractor.find()
        .onErrorResumeNext {
          Completable.fromAction { view.showCreateAnimation() }
              .subscribeOn(viewScheduler)
              .andThen(createWallet())
        }
        .flatMap { smsValidationRepository.isValid(it.address) }.subscribeOn(
            networkScheduler).observeOn(viewScheduler)
        .doOnSuccess {
          if (it == WalletValidationStatus.SUCCESS) {
            view.closeSuccess()
          } else {
            view.showPhoneValidationView(null, null)
          }
        }
        .subscribe({}, {
          it.printStackTrace()
          view.closeError()
        }))
  }

  private fun createWallet(): Single<Wallet> {
    return createWalletInteractor.create()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { view.hideAnimation() }
  }

  fun stop() {
    disposables.clear()
  }
}
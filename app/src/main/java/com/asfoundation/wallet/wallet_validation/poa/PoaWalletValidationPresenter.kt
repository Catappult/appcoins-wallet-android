package com.asfoundation.wallet.wallet_validation.poa

import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.SmsValidationRepositoryType
import com.asfoundation.wallet.service.AccountWalletService
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus.SUCCESS
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class PoaWalletValidationPresenter(
    private val view: PoaWalletValidationView,
    private val smsValidationRepository: SmsValidationRepositoryType,
    private val accountWalletService: AccountWalletService,
    private val disposables: CompositeDisposable,
    private val viewScheduler: Scheduler,
    private val networkScheduler: Scheduler
) {

  fun present() {
    handleWalletValidation()
  }

  private fun handleWalletValidation() {
    disposables.add(accountWalletService.findWalletOrCreate().doOnNext {
      when (it) {
        "Creating" -> view.showCreateAnimation()
      }
    }.filter { it != "Creating" }.flatMap {
      smsValidationRepository.isValid(it)
          .toObservable()
          .subscribeOn(
              networkScheduler)
          .observeOn(viewScheduler)
    }.map {
      if (it == SUCCESS) {
        view.closeSuccess()
      } else {
        view.showPhoneValidationView(null, null)
      }
    }.subscribe({}, {
      it.printStackTrace()
      view.closeError()
    }))
  }

  // TODO remove this also
  private fun createWallet(): Single<Wallet> {
    return accountWalletService.create()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { view.hideAnimation() }
  }

  fun stop() {
    disposables.clear()
  }
}
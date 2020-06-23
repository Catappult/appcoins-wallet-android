package com.asfoundation.wallet.wallet_validation.poa

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.repository.SmsValidationRepositoryType
import com.asfoundation.wallet.service.WalletGetterStatus
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus.SUCCESS
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class PoaWalletValidationPresenter(
    private val view: PoaWalletValidationView,
    private val smsValidationRepository: SmsValidationRepositoryType,
    private val accountWalletService: WalletService,
    private val disposables: CompositeDisposable,
    private val viewScheduler: Scheduler,
    private val networkScheduler: Scheduler
) {

  fun present() {
    handleWalletValidation()
  }

  private fun handleWalletValidation() {
    disposables.add(accountWalletService.findWalletOrCreate().doOnNext {
      if (it == WalletGetterStatus.CREATING.toString()) {
        view.showCreateAnimation()
      }
    }.filter { it != WalletGetterStatus.CREATING.toString() }.flatMap {
      smsValidationRepository.isValid(it)
          .toObservable()
          .subscribeOn(networkScheduler)
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

  fun stop() {
    disposables.clear()
  }
}
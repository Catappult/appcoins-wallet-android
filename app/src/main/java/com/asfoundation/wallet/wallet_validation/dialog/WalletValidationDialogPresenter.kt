package com.asfoundation.wallet.wallet_validation.dialog

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.repository.SmsValidationRepositoryType
import com.asfoundation.wallet.service.WalletGetterStatus
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus.SUCCESS
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class WalletValidationDialogPresenter(
    private val dialogView: WalletValidationDialogView,
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
    disposables.add(accountWalletService.findWalletOrCreate()
        .doOnNext {
          if (it == WalletGetterStatus.CREATING.toString()) {
            dialogView.showCreateAnimation()
          }
        }
        .filter { it != WalletGetterStatus.CREATING.toString() }
        .flatMap {
          smsValidationRepository.isValid(it)
              .toObservable()
              .subscribeOn(networkScheduler)
              .observeOn(viewScheduler)
        }
        .map {
          if (it == SUCCESS) {
            dialogView.closeSuccess()
          } else {
            dialogView.showPhoneValidationView(null, null)
          }
        }
        .subscribe({}, {
          it.printStackTrace()
          dialogView.closeError()
        }))
  }

  fun stop() {
    disposables.clear()
  }
}
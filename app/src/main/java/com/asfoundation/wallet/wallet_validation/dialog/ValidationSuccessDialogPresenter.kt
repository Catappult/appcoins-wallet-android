package com.asfoundation.wallet.wallet_validation.dialog

import com.asfoundation.wallet.poa.ProofOfAttentionService
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable

class ValidationSuccessDialogPresenter(
    private val dialogView: ValidationSuccessDialogView,
    private val service: ProofOfAttentionService,
    private val disposables: CompositeDisposable,
    private val activity: WalletValidationDialogView?
) {

  fun present() {
    dialogView.setupUI()
    handleAnimationEnd()
  }

  private fun handleAnimationEnd() {
    disposables.add(
        dialogView.handleAnimationEnd()
            .filter { animationCompleted -> animationCompleted }
            .flatMapCompletable { Completable.fromAction { service.setWalletValidated() } }
            .subscribe({}, {
              it.printStackTrace()
              activity?.closeSuccess()
            })
    )
  }

  fun stop() {
    disposables.clear()
    dialogView.clean()
  }

}

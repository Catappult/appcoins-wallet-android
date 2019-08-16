package com.asfoundation.wallet.poa_wallet_validation

import com.asfoundation.wallet.poa.ProofOfAttentionService
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable

class ValidationSuccessPresenter(
    private val view: ValidationSuccessView,
    private val service: ProofOfAttentionService,
    private val disposables: CompositeDisposable,
    private val activity: WalletValidationView?
) {

  fun present() {
    view.setupUI()
    handleAnimationEnd()
  }

  private fun handleAnimationEnd() {
    disposables.add(
        view.handleAnimationEnd()
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
    view.clean()
  }

}

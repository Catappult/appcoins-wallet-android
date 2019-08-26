package com.asfoundation.wallet.wallet_validation.poa

import com.asfoundation.wallet.poa.ProofOfAttentionService
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable

class PoaValidationSuccessPresenter(
    private val view: PoaValidationSuccessView,
    private val service: ProofOfAttentionService,
    private val disposables: CompositeDisposable,
    private val activity: PoaWalletValidationView?
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

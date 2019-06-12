package com.asfoundation.wallet.wallet_validation

import com.asfoundation.wallet.poa.ProofOfAttentionService
import io.reactivex.disposables.CompositeDisposable

class ValidationSuccessPresenter(
    private val view: ValidationSuccessFragment,
    private val service: ProofOfAttentionService
) {

  private val disposables: CompositeDisposable = CompositeDisposable()

  fun present() {
    view.setupUI()
  }

  fun updatePoA() {
    service.setWalletValidated()
  }

  fun stop() {
    disposables.clear()
    view.clean()
  }

}

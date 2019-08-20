package com.asfoundation.wallet.wallet_validation.generic

import com.asfoundation.wallet.interact.CreateWalletInteract
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.repository.SmsValidationRepositoryType
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class WalletValidationPresenter(
    private val view: WalletValidationView,
    private val smsValidationRepository: SmsValidationRepositoryType,
    private val walletInteractor: FindDefaultWalletInteract,
    private val createWalletInteractor: CreateWalletInteract,
    private val disposables: CompositeDisposable,
    private val viewScheduler: Scheduler,
    private val networkScheduler: Scheduler
) {

  fun present() {
    view.showPhoneValidationView()
  }

  fun stop() {
    disposables.clear()
  }
}
package com.asfoundation.wallet.withdraw.ui

import com.asfoundation.wallet.withdraw.repository.WithdrawRepository
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class WithdrawPresenter(private val view: WithdrawView,
                        private val withdrawRepository: WithdrawRepository,
                        private val compositeDisposable: CompositeDisposable,
                        private val scheduler: Scheduler,
                        private val viewScheduler: Scheduler) {

  fun present() {
    compositeDisposable.add(view.getWithdrawClicks()
        .observeOn(scheduler)
        .flatMapCompletable { withdrawRepository.withdrawAppcCredits(it.first, it.second) }
        .observeOn(viewScheduler)
        .doOnError { view.showError(it) }
        .retry()
        .subscribe { view.showWithdrawSuccessMessage() })

  }

  fun stop() {
    compositeDisposable.dispose()
  }

}

package com.asfoundation.wallet.withdraw.ui

import com.asfoundation.wallet.withdraw.WithdrawResult
import com.asfoundation.wallet.withdraw.usecase.WithdrawFiatUseCase
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class WithdrawPresenter(
  private val view: WithdrawView,
  private val withdrawUseCase: WithdrawFiatUseCase,
  private val compositeDisposable: CompositeDisposable,
  private val scheduler: Scheduler,
  private val viewScheduler: Scheduler
) {

  fun present() {
    compositeDisposable.add(view.getWithdrawClicks()
      .doOnNext { view.showLoading() }
      .observeOn(scheduler)
      .flatMapSingle { withdrawUseCase.execute(it.first, it.second) }
      .observeOn(viewScheduler)
      .doOnError {
        it.printStackTrace()
        view.showError(it)
        view.hideLoading()
      }
      .doOnNext {
        when (it.status) {
          WithdrawResult.Status.SUCCESS -> view.showWithdrawSuccessMessage()
          WithdrawResult.Status.NOT_ENOUGH_EARNING -> view.showNotEnoughEarningsBalanceError()
          WithdrawResult.Status.NOT_ENOUGH_BALANCE -> view.showNotEnoughBalanceError()
          WithdrawResult.Status.NO_NETWORK -> view.showNoNetworkError()
          WithdrawResult.Status.INVALID_EMAIL -> view.showInvalidEmailError()
        }
      }
      .doOnNext { view.hideLoading() }
      .retry()
      .subscribe())

  }

  fun stop() {
    compositeDisposable.dispose()
  }

}

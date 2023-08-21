package com.asfoundation.wallet.ui.iab.payments.common.error

import com.wallet.appcoins.feature.support.data.SupportInteractor
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class IabErrorPresenter(
    private val view: IabErrorView,
    private val data: IabErrorData,
    private val navigator: IabErrorNavigator,
    private val supportInteractor: com.wallet.appcoins.feature.support.data.SupportInteractor,
    private val disposables: CompositeDisposable) {

  fun present() {
    initializeView()
    handleBackClick()
    handleCancelClick()
    handleSupportClick()
  }

  private fun initializeView() {
    view.setErrorMessage(data.errorMessage)
  }

  private fun handleCancelClick() {
    disposables.add(
        view.cancelClickEvent()
            .doOnNext { navigator.cancelPayment() }
            .retry()
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleBackClick() {
    disposables.add(
        view.backClickEvent()
            .doOnNext { navigator.navigateBackToPayment(data.backStackEntryName) }
            .retry()
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleSupportClick() {
    disposables.add(Observable.merge(view.getSupportIconClicks(), view.getSupportLogoClicks())
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .flatMapCompletable { supportInteractor.showSupport() }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  fun stop() = disposables.clear()
}
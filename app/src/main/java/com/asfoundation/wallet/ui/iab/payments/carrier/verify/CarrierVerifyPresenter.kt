package com.asfoundation.wallet.ui.iab.payments.carrier.verify

import com.asfoundation.wallet.util.applicationinfo.ApplicationInfoLoader
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class CarrierVerifyPresenter(
    private val disposables: CompositeDisposable,
    private val view: CarrierVerifyView,
    private val data: CarrierVerifyData,
    private val navigator: CarrierVerifyNavigator,
    private val appInfoLoader: ApplicationInfoLoader) {

  fun present() {
    initializeView()
    handleBackButton()
    handleNextButton()
  }

  private fun initializeView() {
    disposables.add(
        appInfoLoader.getApplicationInfo(data.domain)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { ai ->
              view.initializeView(ai.appName, ai.icon, data.currency, data.fiatAmount,
                  data.appcAmount, data.skuDescription, data.bonusAmount)
            }
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleNextButton() {
    disposables.add(
        view.nextClickEvent()
            .doOnNext { navigator.navigateToConfirm() }
            .retry()
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }


  private fun handleBackButton() {
    disposables.add(
        view.backEvent()
            .doOnNext { navigator.navigateBack() }
            .retry()
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  fun stop() = disposables.clear()

}
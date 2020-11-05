package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import com.asfoundation.wallet.util.applicationinfo.ApplicationInfoLoader
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class CarrierConfirmPresenter(private val disposables: CompositeDisposable,
                              private val view: CarrierConfirmView,
                              private val data: CarrierConfirmData,
                              private val navigator: CarrierConfirmNavigator,
                              private val appInfoLoader: ApplicationInfoLoader,
                              private val viewScheduler: Scheduler,
                              private val ioScheduler: Scheduler) {
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
              view.initializeView(ai.appName, ai.icon, data.currency, data.totalFiatAmount,
                  data.totalAppcAmount, data.skuDescription, data.bonusAmount, data.carrierName,
                  data.carrierImage, data.feeFiatAmount)
            }
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleNextButton() {
    disposables.add(
        view.nextClickEvent()
            .doOnNext {
              view.setLoading()
              navigator.navigateToWebview()
            }
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
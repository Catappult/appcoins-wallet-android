package com.asfoundation.wallet.ui.iab.payments.carrier.verify

import com.asfoundation.wallet.util.applicationinfo.ApplicationInfoLoader
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal

class CarrierVerifyPresenter(
    private val disposables: CompositeDisposable,
    private val view: CarrierVerifyView,
    private val data: CarrierVerifyData,
    private val navigator: CarrierVerifyNavigator,
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
            .observeOn(viewScheduler)
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
            .observeOn(viewScheduler)
            .doOnNext {
              view.setLoading()
              navigator.navigateToConfirm(data.domain, data.transactionData, data.currency,
                  data.fiatAmount, data.appcAmount, data.bonusAmount, data.skuDescription,
                  BigDecimal.ONE, "Vodafone",
                  "https://img.favpng.com/1/22/1/vodafone-uk-telecommunication-iphone-logo-png-favpng-CUqzJiT1AykZUYrNuvQz0S9va.jpg")
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
package com.asfoundation.wallet.promotions.voucher

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class VoucherDetailsPresenter(private val view: VoucherDetailsView,
                              private val disposable: CompositeDisposable,
                              private val interactor: VoucherDetailsInteractor,
                              private val navigator: VoucherDetailsNavigator,
                              private val data: VoucherDetailsData,
                              private val viewScheduler: Scheduler,
                              private val ioScheduler: Scheduler) {

  fun present() {
    view.setupUi(data.title, data.featureGraphic, data.icon, data.maxBonus, data.packageName,
        data.hasAppcoins)
    initializeView()
    handleNextClick()
    handleCancelClick()
    handleBackClick()
    handleSkuButtonClick()
    handleDownloadAppButtonClick()
  }

  private fun handleDownloadAppButtonClick() {
    disposable.add(view.onDownloadButtonClick()
        .doOnNext { navigator.navigateToStore(data.packageName) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleSkuButtonClick() {
    disposable.add(view.onSkuButtonClick()
        .doOnNext { index -> view.setSelectedSku(index) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleBackClick() {
    disposable.add(view.onBackPressed()
        .doOnNext { navigator.navigateBack() }
        .subscribe({ }, { it.printStackTrace() }))
  }

  private fun handleCancelClick() {
    disposable.add(view.onCancelClicks()
        .doOnNext { navigator.navigateBack() }
        .subscribe({ }, { it.printStackTrace() }))
  }

  private fun handleNextClick() {
    disposable.add(view.onNextClicks()
        .filter { it.error.not() }
        .doOnNext { navigator.navigateToPurchaseFlow() }
        .subscribe({ }, { it.printStackTrace() }))
  }

  private fun initializeView() {
    disposable.add(interactor.getSkuButtonModels()
        .subscribeOn(ioScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { view.setupSkus(it) }
        .subscribe({ }, { it.printStackTrace() }))
  }

  fun stop() = disposable.clear()
}
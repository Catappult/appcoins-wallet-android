package com.asfoundation.wallet.promotions.voucher

import com.asfoundation.wallet.logging.Logger
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class VoucherDetailsPresenter(private val view: VoucherDetailsView,
                              private val disposable: CompositeDisposable,
                              private val interactor: VoucherDetailsInteractor,
                              private val navigator: VoucherDetailsNavigator,
                              private val data: VoucherDetailsData,
                              private val viewScheduler: Scheduler,
                              private val ioScheduler: Scheduler,
                              private val logger: Logger) {

  fun present() {
    view.showLoading()
    view.setupUi(data.title, data.featureGraphic, data.icon, data.maxBonus, data.packageName,
        data.hasAppcoins)
    retrieveSkuList()
    handleNextClick()
    handleCancelClick()
    handleBackClick()
    handleSkuButtonClick()
    handleDownloadAppButtonClick()
    handleRetryClick()
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
        .doOnNext { navigator.navigateToPurchaseFlow(it) }
        .subscribe({ }, { it.printStackTrace() }))
  }

  private fun handleRetryClick() {
    disposable.add(view.onRetryClick()
        .observeOn(viewScheduler)
        .doOnNext { view.showRetryAnimation() }
        .delay(1, TimeUnit.SECONDS)
        .observeOn(viewScheduler)
        .doOnNext { retrieveSkuList() }
        .subscribe({}, { it.printStackTrace() }))
  }


  private fun retrieveSkuList() {
    disposable.add(interactor.getVoucherSkus()
        .subscribeOn(ioScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          if (it.error.hasError.not()) {
            view.hideLoading()
            view.setupSkus(it.list)
          } else if (it.error.isNoNetwork) {
            view.showNoNetworkError()
          }
        }
        .subscribe({ }, { logger.log(TAG, it) }))
  }

  fun stop() = disposable.clear()

  private companion object {
    private val TAG = VoucherDetailsPresenter::class.java.simpleName
  }
}
package com.asfoundation.wallet.promotions.voucher

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class EVoucherDetailsPresenter(
    private val view: EVoucherDetailsView,
    private val interactor: EVoucherDetailsInteractor,
    private val navigator: EVoucherDetailsNavigator,
    private val data: EVoucherDetailsData
) {

  private val disposable = CompositeDisposable()

  fun present() {
    initializeView()
    handleNextClick()
    handleCancelClick()
    handleBackClick()
    handleSkuButtonClick()
    handleDownloadAppButtonClick()
  }

  private fun handleDownloadAppButtonClick() {
    disposable.add(view.onDownloadAppButtonClick()
        .subscribe({ any -> navigator.navigateToStore(data.packageName) }))
  }

  private fun handleSkuButtonClick() {
    disposable.add(view.onSkuButtonClick()
        .subscribe({ index -> view.setSelectedSku(index) }))
  }

  private fun handleBackClick() {
    disposable.add(view.onBackPressed()
        .subscribe({ navigator.navigateBack() }))
  }

  private fun handleCancelClick() {
    disposable.add(view.onCancelClicks()
        .subscribe({ navigator.navigateBack() }))
  }

  private fun handleNextClick() {
    disposable.add(view.onNextClicks()
        .subscribe({ navigator.navigateToNextScreen() }))
  }

  private fun initializeView() {
    disposable.add(interactor.getSkuButtonModels()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess({ skuModels -> view.setupUi(data.title, data.packageName, skuModels) })
        .subscribe({ skuModels -> }, { t -> t.printStackTrace() }))
  }

  fun stop() {
    disposable.clear()
  }
}
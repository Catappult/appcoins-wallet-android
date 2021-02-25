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
    disposable.add(interactor.getSkuButtonModels()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ skuModels ->
          view.setupUi(data.title, data.packageName, skuModels)
        }))
    disposable.add(view.onNextClicks()
        .subscribe({ navigator.navigateToNextScreen() }))
    disposable.add(view.onCancelClicks()
        .subscribe({ navigator.navigateBack() }))
    disposable.add(view.onBackPressed()
        .subscribe({ navigator.navigateBack() }))
    disposable.add(view.onSkuButtonClick()
        .subscribe({ index -> view.setSelectedSku(index) }))
    disposable.add(view.onDownloadAppButtonClick()
        .subscribe({ any -> navigator.navigateToStore(data.packageName) }))
  }
}
package com.asfoundation.wallet.promotions.voucher

import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class EVoucherDetailsPresenter(
    private val view: EVoucherDetailsView,
    private val interactor: EVoucherDetailsInteractor,
    private val navigator: EVoucherDetailsNavigator,
    private val data: EVoucherDetailsData
) {

  private val disposable = CompositeDisposable()

  fun present() {
    view.setupUi(data.title, data.packageName)
    disposable.add(view.onNextClicks()
        .subscribe({ navigator.navigateToNextScreen() }))
    disposable.add(view.onCancelClicks()
        .subscribe({ navigator.navigateBack() }))
    disposable.add(view.onSkuButtonClick()
        .subscribe({ index -> view.setSelectedSku(index) }))
    disposable.add(view.onDownloadAppButtonClick()
        .subscribe({ any -> navigator.navigateToStore(data.packageName) }))
  }

  fun getDiamondModels(): Single<List<SkuButtonModel>> {
    return interactor.getDiamondModels()
  }
}
package com.asfoundation.wallet.promotions.voucher

import io.reactivex.disposables.CompositeDisposable

class EVoucherDetailsPresenter(
    private val view: EVoucherDetailsView,
    private val interactor: EVoucherDetailsInteractor,
    private val navigator: EVoucherDetailsNavigator
) {

  private val disposable = CompositeDisposable()

  fun present() {
    view.setupUi(interactor.getTitle())
    disposable.add(view.onNextClicks()
        .subscribe({ navigator.navigateToNextScreen() }))
    disposable.add(view.onCancelClicks()
        .subscribe({ navigator.navigateBack() }))
  }

  fun getDiamondModels(): List<DiamondsButtonModel> {
    return interactor.getDiamondModels()
  }
}
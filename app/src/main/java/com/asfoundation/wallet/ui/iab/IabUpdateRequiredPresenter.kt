package com.asfoundation.wallet.ui.iab

import com.asfoundation.wallet.update_required.use_cases.BuildUpdateIntentUseCase
import io.reactivex.disposables.CompositeDisposable

class IabUpdateRequiredPresenter(
  private val view: IabUpdateRequiredView,
  private val disposables: CompositeDisposable,
  private val buildUpdateIntentUseCase: BuildUpdateIntentUseCase
) {

  fun present() {
    handleUpdateClick()
    handleCancelClick()
  }

  private fun handleCancelClick() {
    disposables.add(view.cancelClick()
      .doOnNext { view.close() }
      .subscribe())
  }

  private fun handleUpdateClick() {
    disposables.add(view.updateClick()
      .doOnNext { view.navigateToIntent(buildUpdateIntentUseCase()) }
      .subscribe({}, { handleError(it) })
    )
  }

  private fun handleError(throwable: Throwable) {
    throwable.printStackTrace()
    view.showError()
  }

  fun stop() {
    disposables.clear()
  }

}

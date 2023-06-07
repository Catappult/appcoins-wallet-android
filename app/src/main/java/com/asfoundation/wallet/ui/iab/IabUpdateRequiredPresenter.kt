package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletsModelUseCase
import com.asfoundation.wallet.update_required.use_cases.BuildUpdateIntentUseCase
import io.reactivex.disposables.CompositeDisposable

class IabUpdateRequiredPresenter(
        private val view: IabUpdateRequiredView,
        private val disposables: CompositeDisposable,
        private val buildUpdateIntentUseCase: BuildUpdateIntentUseCase,
        private val getWalletsModelUseCase: GetWalletsModelUseCase,
        private val rxSchedulers: RxSchedulers
) {

  fun present() {
    handleUpdateClick()
    handleCancelClick()
    handleBackupClick()
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

  private fun handleBackupClick() {
    disposables.add(view.backupClick()
      .flatMapSingle {
        getWalletsModelUseCase()
          .observeOn(rxSchedulers.main)
          .doOnSuccess { walletsModel ->
            when (walletsModel.totalWallets) {
              0 -> Unit
              1 -> view.navigateToBackup(walletAddress = walletsModel.wallets[0].walletAddress)
              else -> view.setDropDownMenu(walletsModel)
            }
          }
      }
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

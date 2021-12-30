package com.asfoundation.wallet.ui.backup.creation

import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.ui.backup.use_cases.SendBackupToEmailUseCase
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class BackupCreationPresenter(private val view: BackupCreationView,
                              private val interactor: BackupCreationInteractor,
                              private val walletsEventSender: WalletsEventSender,
                              private val logger: Logger,
                              private val viewScheduler: Scheduler,
                              private val disposables: CompositeDisposable,
                              private val data: BackupCreationData,
                              private val navigator: BackupCreationNavigator,
                              private val sendBackupToEmailUseCase: SendBackupToEmailUseCase) {

  companion object {
    private val TAG = BackupCreationPresenter::class.java.name
  }

  fun present() {
    handleSendToEmailClick()
    handleSaveOnDeviceClick()
    handlePermissionGiven()
  }

  private fun handleSendToEmailClick() {
    disposables.add(view.getSendToEmailClick()
        .observeOn(viewScheduler)
        .flatMap {
          sendBackupToEmailUseCase(data.walletAddress, data.password, it).andThen(
              Observable.just(it))
        }
        .doOnNext {
          navigator.navigateToSuccessScreen()
        }
        .subscribe({}) { showError(it) }
    )
  }

  private fun handleSaveOnDeviceClick() {
    disposables.add(view.getSaveOnDeviceButton()
        .doOnNext { view.askForWritePermissions() }
        .doOnNext {
          walletsEventSender.sendWalletConfirmationBackupEvent(WalletsAnalytics.ACTION_SAVE)
          interactor.saveBackedUpOnce()
        }
        .subscribe({}, { logger.log(TAG, it) }))
  }

  private fun handlePermissionGiven() {
    disposables.add(view.onPermissionGiven()
        .doOnNext {
          navigator.navigateToSaveOnDeviceScreen(data.walletAddress, data.password)
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() {
    disposables.clear()
  }

  private fun showError(throwable: Throwable) {
    throwable.printStackTrace()
    logger.log(TAG, throwable)
    view.showError()
  }

  private fun showError(message: String) {
    logger.log(TAG, message)
    view.showError()
  }
}

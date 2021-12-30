package com.asfoundation.wallet.ui.backup.creation

import android.os.Bundle
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.ui.backup.use_cases.SendBackupToEmailUseCase
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
    private const val FILE_SHARED_KEY = "FILE_SHARED"
    private const val KEYSTORE_KEY = "KEYSTORE"
    private const val FILE_NAME_KEY = "FILE_NAME"
    private val TAG = BackupCreationPresenter::class.java.name
  }

  private var fileShared = false
  private var cachedKeystore = ""
  private var cachedFileName: String? = null

  fun present(savedInstance: Bundle?) {
    savedInstance?.let {
      fileShared = it.getBoolean(FILE_SHARED_KEY)
      cachedKeystore = it.getString(KEYSTORE_KEY, "")
      cachedFileName = it.getString(FILE_NAME_KEY)
    }
    handleSendToEmailClick()
    handleSaveOnDeviceClick()
    handlePermissionGiven()
  }

  private fun handleSendToEmailClick() {
    disposables.add(view.getSendToEmailClick()
        .observeOn(viewScheduler)
        .doOnNext {
          sendBackupToEmailUseCase(data.walletAddress, data.password, it)
        }
        .doOnComplete {
          view.closeScreen()
        }
        .subscribe({}, { view.closeScreen() })
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
          navigator.showSaveOnDeviceScreen(data.walletAddress, data.password)
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() {
    interactor.deleteFile()
    disposables.clear()
  }

  private fun showError(message: String) {
    logger.log(TAG, message)
    view.showError()
  }
}

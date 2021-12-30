package com.asfoundation.wallet.ui.backup.creation

import android.content.Context
import android.os.Build
import android.os.Environment
import com.asfoundation.wallet.backup.FileInteractor
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.interact.ExportWalletInteractor
import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.repository.BackupRestorePreferencesRepository
import com.asfoundation.wallet.ui.backup.BackupActivityNavigator
import com.asfoundation.wallet.ui.backup.creation.BackupCreationFragment.Companion.PASSWORD_KEY
import com.asfoundation.wallet.ui.backup.creation.BackupCreationFragment.Companion.WALLET_ADDRESS_KEY
import com.asfoundation.wallet.ui.backup.use_cases.SendBackupToEmailUseCase
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Named

@Module
class BackupCreationModule {

  @Provides
  fun providesBackupCreationPresenter(fragment: BackupCreationFragment,
                                      backupCreationInteractor: BackupCreationInteractor,
                                      walletsEventSender: WalletsEventSender,
                                      logger: Logger,
                                      data: BackupCreationData,
                                      navigator: BackupCreationNavigator,
                                      @Named("temporary-path") temporaryPath: File?,
                                      @Named("downloads-path") downloadsPath: File?,
                                      sendBackupToEmailUseCase: SendBackupToEmailUseCase)
      : BackupCreationPresenter {
    return BackupCreationPresenter(fragment as BackupCreationView, backupCreationInteractor,
        walletsEventSender, logger, Schedulers.io(), AndroidSchedulers.mainThread(),
        CompositeDisposable(), data, navigator, temporaryPath, downloadsPath,
        sendBackupToEmailUseCase)
  }

  @Provides
  fun providesBackupCreationData(fragment: BackupCreationFragment): BackupCreationData {
    fragment.arguments!!.apply {
      return BackupCreationData(getString(WALLET_ADDRESS_KEY)!!, getString(PASSWORD_KEY)!!)
    }
  }

  @Provides
  fun providesBackupCreationInteractor(exportWalletInteractor: ExportWalletInteractor,
                                       fileInteractor: FileInteractor,
                                       backupRestorePreferencesRepository: BackupRestorePreferencesRepository): BackupCreationInteractor {
    return BackupCreationInteractor(exportWalletInteractor, fileInteractor,
        backupRestorePreferencesRepository)
  }

  @Provides
  @Named("temporary-path")
  fun providesTemporaryPath(context: Context): File? = context.externalCacheDir

  @Provides
  @Named("downloads-path")
  fun providesDownloadsPath(): File? {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS)
    else null
  }

  @Provides
  fun providesBackupCreationNavigator(fragment: BackupCreationFragment,
                                      activityNavigator: BackupActivityNavigator): BackupCreationNavigator {
    return BackupCreationNavigator(fragment.requireFragmentManager(), activityNavigator)
  }

  @Provides
  fun providesBackupActivityNavigator(fragment: BackupCreationFragment): BackupActivityNavigator {
    return BackupActivityNavigator(fragment.requireFragmentManager(), fragment.activity!!)
  }
}
package com.asfoundation.wallet.ui.backup.creation

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

@Module
class BackupCreationModule {

  @Provides
  fun providesBackupCreationPresenter(fragment: BackupCreationFragment,
                                      backupCreationInteractor: BackupCreationInteractor,
                                      walletsEventSender: WalletsEventSender,
                                      logger: Logger,
                                      data: BackupCreationData,
                                      navigator: BackupCreationNavigator,
                                      sendBackupToEmailUseCase: SendBackupToEmailUseCase)
      : BackupCreationPresenter {
    return BackupCreationPresenter(fragment as BackupCreationView, backupCreationInteractor,
        walletsEventSender, logger, AndroidSchedulers.mainThread(), CompositeDisposable(), data,
        navigator,
        sendBackupToEmailUseCase)
  }

  @Provides
  fun providesBackupCreationData(fragment: BackupCreationFragment): BackupCreationData {
    fragment.arguments!!.apply {
      return BackupCreationData(getString(WALLET_ADDRESS_KEY)!!, getString(PASSWORD_KEY)!!)
    }
  }

  @Provides
  fun providesBackupCreationInteractor(
      backupRestorePreferencesRepository: BackupRestorePreferencesRepository): BackupCreationInteractor {
    return BackupCreationInteractor(backupRestorePreferencesRepository)
  }

  @Provides
  fun providesBackupCreationNavigator(fragment: BackupCreationFragment): BackupCreationNavigator {
    return BackupCreationNavigator(fragment.requireFragmentManager())
  }

  @Provides
  fun providesBackupActivityNavigator(fragment: BackupCreationFragment): BackupActivityNavigator {
    return BackupActivityNavigator(fragment.requireFragmentManager(), fragment.activity!!)
  }
}
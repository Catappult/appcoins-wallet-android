package com.asfoundation.wallet.backup.save_on_device

import android.os.Build
import android.os.Environment
import androidx.fragment.app.Fragment
import com.asfoundation.wallet.backup.use_cases.BackupSuccessLogUseCase
import com.asfoundation.wallet.backup.use_cases.SaveBackupFileUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import java.io.File
import javax.inject.Named

@InstallIn(FragmentComponent::class)
@Module
class BackupSaveOnDeviceDialogModule {

  @Provides
  fun providesBackupSaveOnDeviceDialogViewModelFactory(
    backupSaveOnDeviceDialogData: BackupSaveOnDeviceDialogData,
    saveBackupFileUseCase: SaveBackupFileUseCase,
    backupSuccessLogUseCase: BackupSuccessLogUseCase,
    @Named("downloads-path") downloadsPath: File?
  ): BackupSaveOnDeviceDialogViewModelFactory {
    return BackupSaveOnDeviceDialogViewModelFactory(
      backupSaveOnDeviceDialogData, saveBackupFileUseCase,
      backupSuccessLogUseCase, downloadsPath
    )
  }

  @Provides
  fun providesBackupSaveOnDeviceDialogData(
    fragment: Fragment
  ): BackupSaveOnDeviceDialogData {
    fragment.requireArguments()
      .apply {
        return BackupSaveOnDeviceDialogData(
          getString(BackupSaveOnDeviceDialogFragment.WALLET_ADDRESS_KEY)!!, getString(
            BackupSaveOnDeviceDialogFragment.PASSWORD_KEY
          )!!
        )
      }
  }

  @Provides
  @Named("downloads-path")
  fun providesDownloadsPath(): File? {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS)
    else null
  }
}
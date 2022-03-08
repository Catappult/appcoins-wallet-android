package com.asfoundation.wallet.ui.backup.save_on_device

import android.os.Build
import android.os.Environment
import androidx.fragment.app.Fragment
import com.asfoundation.wallet.ui.backup.use_cases.BackupSuccessLogUseCase
import com.asfoundation.wallet.ui.backup.use_cases.SaveBackupFileUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import java.io.File
import javax.inject.Named

@InstallIn(FragmentComponent::class)
@Module
class SaveOnDeviceDialogModule {

  @Provides
  fun providesSaveOnDeviceDialogViewModelFactory(
    saveOnDeviceDialogData: SaveOnDeviceDialogData,
    saveBackupFileUseCase: SaveBackupFileUseCase,
    backupSuccessLogUseCase: BackupSuccessLogUseCase,
    @Named("downloads-path") downloadsPath: File?
  ): SaveOnDeviceDialogViewModelFactory {
    return SaveOnDeviceDialogViewModelFactory(
      saveOnDeviceDialogData, saveBackupFileUseCase,
      backupSuccessLogUseCase, downloadsPath
    )
  }

  @Provides
  fun providesSaveOnDeviceDialogData(
    fragment: Fragment
  ): SaveOnDeviceDialogData {
    fragment.requireArguments()
      .apply {
        return SaveOnDeviceDialogData(
          getString(SaveOnDeviceDialogFragment.WALLET_ADDRESS_KEY)!!, getString(
            SaveOnDeviceDialogFragment.PASSWORD_KEY
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
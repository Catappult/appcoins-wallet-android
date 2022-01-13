package com.asfoundation.wallet.ui.backup.save_on_device

import android.os.Build
import android.os.Environment
import com.asfoundation.wallet.ui.backup.success.BackupSuccessLogUseCase
import com.asfoundation.wallet.ui.backup.use_cases.SaveBackupFileUseCase
import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Named

@Module
class SaveOnDeviceDialogModule {

  @Provides
  fun providesSaveBackupBottomSheetViewModelFactory(
      saveOnDeviceDialogData: SaveOnDeviceDialogData,
      saveBackupFileUseCase: SaveBackupFileUseCase,
      backupSuccessLogUseCase: BackupSuccessLogUseCase,
      @Named("downloads-path") downloadsPath: File?): SaveOnDeviceDialogViewModelFactory {
    return SaveOnDeviceDialogViewModelFactory(saveOnDeviceDialogData, saveBackupFileUseCase,
        backupSuccessLogUseCase, downloadsPath)
  }

  @Provides
  fun providesSaveBackupBottomSheetNavigator(
      fragment: SaveOnDeviceDialogFragment
  ): SaveOnDeviceDialogNavigator {
    return SaveOnDeviceDialogNavigator(fragment, fragment.requireFragmentManager())
  }

  @Provides
  fun providesSaveBackupBottomSheetData(
      fragment: SaveOnDeviceDialogFragment): SaveOnDeviceDialogData {
    fragment.requireArguments()
        .apply {
          return SaveOnDeviceDialogData(
              getString(SaveOnDeviceDialogFragment.WALLET_ADDRESS_KEY)!!, getString(
              SaveOnDeviceDialogFragment.PASSWORD_KEY)!!)
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
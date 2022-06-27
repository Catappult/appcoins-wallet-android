package com.asfoundation.wallet.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.asf.wallet.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import java.io.File
import javax.inject.Inject

class FileInteractor @Inject constructor(
  @ApplicationContext private val context: Context,
  private val contentResolver: ContentResolver
) {

  //If android Q or above, the user must choose the directory so that the file isn't deleted when the app is uninstall
  fun getDownloadPath(): File? =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
      @Suppress("DEPRECATION")
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    } else {
      null
    }

  fun getUriFromFile(file: File): Uri =
    FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)

  fun readFile(fileUri: Uri?): Single<String> = Single
    .fromCallable {
      if (fileUri == null || fileUri.path == null) throw Throwable("Error retrieving file")
      contentResolver.openInputStream(fileUri)
        ?.reader()
        ?.useLines { it.joinToString(separator = "\n", postfix = "\n") }
        ?: ""
    }
    .doOnError(Throwable::printStackTrace)
}

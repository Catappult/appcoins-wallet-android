package com.asfoundation.wallet.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.core.database.getStringOrNull
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.entity.WalletKeyStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import java.io.File
import javax.inject.Inject

/**
 * Max size used to avoid [OutOfMemoryError] when reading file
 */
private const val MAX_FILE_SIZE_BYTES = 1_048_576L // 1 MB

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

  fun readFile(fileUri: Uri?): Single<WalletKeyStore> = Single
    .fromCallable {
      if (fileUri == null || fileUri.path == null) throw Throwable("Error retrieving file")
      var fileName: String? = null
      contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
        cursor.moveToFirst()
        fileName = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
          val fileSize = cursor.getLong(sizeIndex)
          if (fileSize > MAX_FILE_SIZE_BYTES) throw Throwable("File too large to be a valid keystore")
        }
      }
      WalletKeyStore(
        name = fileName?.replace(Regex(".bck$"), ""),
        contents = contentResolver.openInputStream(fileUri)
          ?.bufferedReader()
          ?.use { it.readText() }
          ?: ""
      )
    }
    .doOnError(Throwable::printStackTrace)
}

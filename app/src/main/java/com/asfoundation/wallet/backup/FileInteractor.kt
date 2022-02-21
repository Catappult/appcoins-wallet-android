package com.asfoundation.wallet.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.repository.BackupRestorePreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Completable
import io.reactivex.Single
import java.io.*
import javax.inject.Inject

class FileInteractor @Inject constructor(@ApplicationContext private val context: Context,
                                         private val contentResolver: ContentResolver,
                                         private val backupRestorePreferencesRepository: BackupRestorePreferencesRepository) {

  private var cachedFile: File? = null

  fun createTmpFile(walletAddress: String, content: String, path: File?): Completable {
    val fileName = getDefaultBackupFileName(walletAddress)

    //createTempFile adds numbers in front of filename
    if (path == null) return Completable.error(Throwable("Null path"))
    val file = File.createTempFile("$fileName-", getDefaultBackupFileExtension(), path)

    val fileOutputStream = FileOutputStream(file, false)
    try {
      fileOutputStream.write(content.toByteArray())
      cachedFile = file
    } catch (e: IOException) {
      e.printStackTrace()
      return Completable.error(Throwable(e))
    } finally {
      fileOutputStream.close()
    }
    return Completable.complete()
  }

  //Use this method for android P and below
  fun createAndSaveFile(content: String, path: File?, fileName: String): Completable {
    val stringPath = path?.path ?: return Completable.error(Throwable("Null path"))
    val directory = File("$stringPath${File.separator}AppcoinsBackup")
    directory.mkdirs()
    val file = File("$directory${File.separator}$fileName${getDefaultBackupFileExtension()}")
    val fileOutputStream = FileOutputStream(file, false)
    try {
      fileOutputStream.write(content.toByteArray())
    } catch (e: IOException) {
      e.printStackTrace()
      return Completable.error(Throwable(e))
    } finally {
      fileOutputStream.close()
    }
    return Completable.complete()
  }

  //Use this method for Android Q and above
  fun createAndSaveFile(content: String, documentFile: DocumentFile,
                        fileName: String): Completable {
    //mimetype anything so that the file has the .bck extension alone.
    val file = documentFile.createFile("anything", fileName + getDefaultBackupFileExtension())
        ?: return Completable.error(Throwable("Error creating file"))

    val outputStream = contentResolver.openOutputStream(file.uri)
    try {
      outputStream?.run { write(content.toByteArray()) } ?: return Completable.error(
          Throwable("Null outputStream"))
    } catch (e: IOException) {
      e.printStackTrace()
      return Completable.error(Throwable(e))
    } finally {
      outputStream?.close()
    }
    return Completable.complete()
  }

  fun deleteFile() {
    try {
      cachedFile?.delete()
      cachedFile = null
    } catch (e: SecurityException) {
      e.printStackTrace()
    }
  }

  fun getCachedFile(): File? = cachedFile

  //If android Q or above, the user must choose the directory so that the file isn't deleted when the app is uninstall
  fun getDownloadPath(): File? {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS)
    else null
  }

  fun getUriFromFile(file: File): Uri {
    return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
  }

  fun saveChosenUri(uri: Uri) {
    backupRestorePreferencesRepository.saveChosenUri(uri.toString())
  }

  fun readFile(fileUri: Uri?): Single<String> {
    if (fileUri == null || fileUri.path == null) {
      return Single.error(Throwable("Error retrieving file"))
    } else {
      val keystore = StringBuilder("")
      var reader: BufferedReader? = null
      try {
        val inputStream = contentResolver.openInputStream(fileUri)
        reader = BufferedReader(InputStreamReader(inputStream!!))
        var mLine: String?
        while (reader.readLine()
                .also { mLine = it } != null) {
          keystore.append(mLine)
          keystore.append('\n')
        }
        reader.close()
      } catch (e: Exception) {
        e.printStackTrace()
        reader?.close()
        return Single.error(e)
      }
      return Single.just(keystore.toString())
    }
  }

  fun getDefaultBackupFileName(walletAddress: String) = "walletbackup$walletAddress"

  private fun getDefaultBackupFileExtension() = ".bck"
}
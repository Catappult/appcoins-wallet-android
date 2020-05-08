package com.asfoundation.wallet.backup

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.os.Environment
import io.reactivex.Single
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileInteract {

  private var cachedFile: File? = null

  fun createTmpFile(walletAddress: String, content: String, path: File?): Single<Boolean> {
    val fileName = getDefaultBackupFileName(walletAddress)

    //createTempFile adds numbers in front of filename
    if (path == null) return Single.just(false)
    val file = File.createTempFile("$fileName-", getDefaultBackupFileExtension(), path)

    val fileOutputStream = FileOutputStream(file, true)
    try {
      fileOutputStream.write(content.toByteArray())
      cachedFile = file
    } catch (e: IOException) {
      e.printStackTrace()
      return Single.just(false)
    } finally {
      fileOutputStream.close()
    }
    return Single.just(true)
  }

  fun createAndSaveFile(content: String, path: File?, fileName: String): Single<Boolean> {
    val stringPath = path?.path ?: return Single.just(false)
    val file = File("$stringPath/$fileName")

    val fileOutputStream = FileOutputStream(file, false)
    try {
      fileOutputStream.write(content.toByteArray())
    } catch (e: IOException) {
      e.printStackTrace()
      return Single.just(false)
    } finally {
      fileOutputStream.close()
    }
    return Single.just(true)
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

  fun getDefaultBackupFileFullName(walletAddress: String) =
      getDefaultBackupFileName(walletAddress) + getDefaultBackupFileExtension()

  private fun getDefaultBackupFileName(walletAddress: String) = "walletbackup$walletAddress"

  private fun getDefaultBackupFileExtension() = ".txt"

  fun getDownloadPath(context: Context?): File? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context?.getDir(
          Environment.DIRECTORY_DOWNLOADS, MODE_PRIVATE)
    } else {
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    }
  }

  fun getTemporaryPath(context: Context?): File? {
    return context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
  }
}
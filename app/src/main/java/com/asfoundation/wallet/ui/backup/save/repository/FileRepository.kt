package com.asfoundation.wallet.ui.backup.save.repository

import android.content.ContentResolver
import androidx.documentfile.provider.DocumentFile
import io.reactivex.Completable
import java.io.IOException

class FileRepository(private val contentResolver: ContentResolver) {
  fun saveFile(content: String, filePath: DocumentFile?,
               fileName: String): Completable {

    //mimetype anything so that the file has the .bck extension alone.
    val file = filePath?.createFile("anything", fileName + getDefaultBackupFileExtension())
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

  private fun getDefaultBackupFileExtension() = ".bck"
}
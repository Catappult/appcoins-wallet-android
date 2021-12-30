package com.asfoundation.wallet.ui.backup.repository

import android.content.ContentResolver
import androidx.documentfile.provider.DocumentFile
import io.reactivex.Completable
import retrofit2.http.Field
import retrofit2.http.POST
import java.io.IOException

class BackupRepository(private val contentResolver: ContentResolver,
                       private val backupEmailApi: BackupEmailApi) {
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

  fun sendBackupEmail(keystore: String, email: String): Completable {
    return backupEmailApi.sendBackupEmail(email, keystore)
  }

  interface BackupEmailApi {
    @POST("broker/8.20210201/wallet/backup")
    fun sendBackupEmail(@Field("email") email: String,
                        @Field("keystore") keystore: String): Completable
  }
}
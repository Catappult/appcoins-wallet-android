package com.asfoundation.wallet.ui.backup.repository

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.documentfile.provider.DocumentFile
import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.util.convertToBase64
import io.reactivex.Completable
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.IOException

class BackupRepository(private val contentResolver: ContentResolver,
                       private val backupEmailApi: BackupEmailApi,
                       private val rxSchedulers: RxSchedulers,
                       private val walletService: WalletService) {
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
    return walletService.getAndSignCurrentWalletAddress()
        .flatMapCompletable {
          backupEmailApi.sendBackupEmail(it.address, it.signedAddress,
              EmailBody(email, keystore.convertToBase64()))
        }
        .subscribeOn(rxSchedulers.io)
  }

  interface BackupEmailApi {
    @POST("broker/8.20210201/wallet/backup")
    fun sendBackupEmail(
        @Query("wallet.address") walletAddress: String,
        @Query("wallet.signature") walletSignature: String,
        @Body emailBody: EmailBody): Completable
  }
}
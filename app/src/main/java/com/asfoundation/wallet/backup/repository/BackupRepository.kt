package com.asfoundation.wallet.backup.repository

import android.content.ContentResolver
import androidx.documentfile.provider.DocumentFile
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.core.network.backend.api.BackupLogApi
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.microservices.api.broker.BackupEmailApi
import com.appcoins.wallet.core.network.microservices.model.EmailBody
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.android_common.extensions.convertToBase64
import io.reactivex.Completable
import java.io.IOException
import javax.inject.Inject

class BackupRepository @Inject constructor(
  private val contentResolver: ContentResolver,
  private val backupEmailApi: BackupEmailApi,
  private val rxSchedulers: RxSchedulers,
  private val walletService: WalletService,
  private val backupLogApi: BackupLogApi,
  private val ewtObtainer: EwtAuthenticatorService,
) {
  fun saveFile(
    content: String, filePath: DocumentFile?,
    fileName: String
  ): Completable {

    //mimetype anything so that the file has the .bck extension alone.
    val file = filePath?.createFile("anything", fileName + getDefaultBackupFileExtension())
      ?: return Completable.error(Throwable("Error creating file"))

    val outputStream = contentResolver.openOutputStream(file.uri)
    try {
      outputStream?.run { write(content.toByteArray()) } ?: return Completable.error(
        Throwable("Null outputStream")
      )
    } catch (e: IOException) {
      e.printStackTrace()
      return Completable.error(Throwable(e))
    } finally {
      outputStream?.close()
    }
    return Completable.complete()
  }

  private fun getDefaultBackupFileExtension() = ".bck"

  fun sendBackupEmail(walletAddress: String, keystore: String, email: String): Completable {
    return ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
      .flatMapCompletable { ewt ->
        walletService.getAndSignSpecificWalletAddress(walletAddress)
          .flatMapCompletable {
            backupEmailApi.sendBackupEmail(
              walletAddress = it.address, walletSignature = it.signedAddress, authorization = ewt,
              emailBody = EmailBody(email, keystore.convertToBase64())
            )
          }
          .subscribeOn(rxSchedulers.io)
      }
  }

  fun logBackupSuccess(ewt: String): Completable {
    return backupLogApi.logBackupSuccess(ewt)
      .subscribeOn(rxSchedulers.io)
  }
}
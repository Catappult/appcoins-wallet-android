package com.asfoundation.wallet.backup.repository

import android.content.ContentResolver
import androidx.documentfile.provider.DocumentFile
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.core.network.backend.api.BackupLogApi
import com.appcoins.wallet.core.network.microservices.api.broker.BackupEmailApi
import com.appcoins.wallet.core.network.microservices.model.EmailBody
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.core.utils.android_common.extensions.convertToBase64
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class BackupRepository @Inject constructor(
    private val contentResolver: ContentResolver,
    private val backupEmailApi: BackupEmailApi,
    private val dispatchers: Dispatchers,
    private val walletService: WalletService,
    private val backupLogApi: BackupLogApi
) {
  suspend fun saveFile(
      content: String, filePath: DocumentFile?,
      fileName: String
  ): Unit {
    //mimetype anything so that the file has the .bck extension alone.
    val file = filePath?.createFile("anything", fileName + getDefaultBackupFileExtension())
        ?: throw IOException("Error creating file")

    withContext(dispatchers.io) {
      val outputStream = contentResolver.openOutputStream(file.uri)
      try {
        outputStream?.write(content.toByteArray())
            ?: throw IOException("Null outputStream")

      } catch (e: IOException) {
        e.printStackTrace()
        throw e
      } finally {
        outputStream?.close()
      }
    }
  }


  private fun getDefaultBackupFileExtension() = ".bck"

  suspend fun sendBackupEmail(walletAddress: String, keystore: String, email: String): Unit {
    withContext(dispatchers.io) {
      val signedAddress = walletService.getAndSignSpecificWalletAddress(walletAddress).await()
      val address = walletService.getAndSignSpecificWalletAddress(walletAddress).await()
      backupEmailApi.sendBackupEmail(address.address, signedAddress.signedAddress,
          EmailBody(email, keystore.convertToBase64()))
    }
  }


  suspend fun logBackupSuccess(ewt: String): Unit {
    withContext(dispatchers.io) {
      backupLogApi.logBackupSuccess(ewt)
    }
  }
}

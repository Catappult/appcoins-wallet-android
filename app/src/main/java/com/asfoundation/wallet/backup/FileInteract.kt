package com.asfoundation.wallet.backup

import io.reactivex.Single
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileInteract {

  private var cachedFile: File? = null

  fun createTmpFile(walletAddress: String, content: String, path: File?): Single<Boolean> {
    val fileName = "walletbackup$walletAddress"
    cachedFile = File.createTempFile(fileName, ".txt", path)
    val fileOutputStream = FileOutputStream(cachedFile!!, true)
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
    cachedFile?.delete()
  }

  fun getCachedFile(): File? = cachedFile

}
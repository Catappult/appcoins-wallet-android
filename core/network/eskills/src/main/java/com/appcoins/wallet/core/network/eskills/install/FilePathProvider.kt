package cm.aptoide.pt.install

import com.appcoins.wallet.core.network.eskills.downloadmanager.PathProvider
import com.appcoins.wallet.core.network.eskills.room.RoomFileToDownload


class FilePathProvider(val apkPath: String, val obbPath: String, val cachePath: String) :
  PathProvider {

  override fun getFilePathFromFileType(fileToDownload: RoomFileToDownload): String? {
    return when (fileToDownload.fileType) {
      RoomFileToDownload.APK -> apkPath
      RoomFileToDownload.OBB -> obbPath + fileToDownload.packageName + "/"
      RoomFileToDownload.SPLIT -> apkPath + fileToDownload.packageName + "-splits/"
      RoomFileToDownload.GENERIC -> cachePath
      else -> cachePath
    }
  }

}
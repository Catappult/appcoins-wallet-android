package cm.aptoide.pt.download

import com.appcoins.wallet.core.network.eskills.room.RoomFileToDownload


class SplitTypeSubFileTypeMapper {

  fun mapSplitToSubFileType(splitType: String): Int {
    return when (splitType) {
      "FEATURE" -> {
        RoomFileToDownload.FEATURE
      }
      "ASSET" -> {
        RoomFileToDownload.ASSET
      }
      else -> {
        RoomFileToDownload.SUBTYPE_APK
      }
    }
  }
}
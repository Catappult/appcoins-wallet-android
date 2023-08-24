package cm.aptoide.pt.database


import com.appcoins.wallet.core.network.eskills.room.RoomInstallation
import com.appcoins.wallet.core.network.eskills.room.RoomInstalled

class RoomInstallationMapper {
  fun map(installedList: List<RoomInstalled>): List<RoomInstallation> {

    val installationList: MutableList<RoomInstallation> =
      ArrayList()
    for (installed in installedList) {
      installationList.add(map(installed))
    }
    return installationList
  }

  fun map(installed: RoomInstalled): RoomInstallation {
    return RoomInstallation(
      installed.packageName, installed.name,
      installed.icon, installed.versionCode, installed.versionName
    )
  }
}
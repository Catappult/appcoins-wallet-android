package cm.aptoide.skills.usecase

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import javax.inject.Inject

class RetrievePackageVersionUseCase
@Inject
constructor(private val packageManager: PackageManager) {
  operator fun invoke(packageName: String): Pair<String, Long>? {
    return try {
      @Suppress("DEPRECATION")
      val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
      val versionName: String = packageInfo.versionName
      val versionCode: Long =
          if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P) {
            @Suppress("DEPRECATION") packageInfo.versionCode.toLong()
          } else {
            packageInfo.longVersionCode
          }
      Pair(versionName, versionCode)
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
      null
    }
  }
}

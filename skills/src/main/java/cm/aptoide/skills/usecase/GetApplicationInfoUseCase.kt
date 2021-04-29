package cm.aptoide.skills.usecase

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

class GetApplicationInfoUseCase(private val context: Context) {
  fun getApplicationIcon(packageName: String): Drawable? {
    return try {
      context.packageManager.getApplicationIcon(packageName)
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
      null
    }
  }

  fun getApplicationName(packageName: String): String? {
    return try {
      val packageManager = context.packageManager
      val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
      packageManager.getApplicationLabel(applicationInfo) as String
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
      null
    }
  }
}
package cm.aptoide.skills.util

import android.view.Window
import android.view.WindowManager

object DeviceScreenManager {
  fun keepAwake(window: Window) {
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
  }

  fun stopKeepAwake(window: Window) {
    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
  }
}
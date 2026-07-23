package com.asfoundation.wallet.util

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.asf.wallet.R

/**
 * Android 16 (API 36) ignores `windowOptOutEdgeToEdgeEnforcement`, so every window is drawn
 * edge-to-edge and can no longer opt out. These helpers restore the pre-16 look — content kept
 * inside the system bars — by consuming the system-bar insets as padding, and filling the
 * status/navigation-bar gaps with the app's dark bar color instead of the (white) window
 * background that was bleeding through.
 *
 * Applied centrally from [com.asfoundation.wallet.App] to every Activity and every
 * bottom-sheet dialog, so individual screens don't each need to handle insets.
 */
object EdgeToEdgeInsets {

  /** Pads the Activity's content view by the system-bar (and IME) insets. */
  fun apply(activity: Activity) {
    val window = activity.window ?: return
    // Transparent / translucent overlay activities (webCheckout, IAB, gamification, custom-tab
    // login, ...) are intentionally drawn edge-to-edge and manage their own layout/insets.
    // Padding or recoloring their window breaks the transparent modal — leave them alone.
    if (activity.hasTransparentWindow()) return
    // Fill the status/navigation-bar gaps with the dark bar color (old statusBarColor),
    // otherwise the white windowBackground shows through on API 36.
    window.decorView.setBackgroundColor(
      ContextCompat.getColor(activity, R.color.styleguide_dark)
    )
    // Dark bar background -> keep the system icons light so they stay visible.
    WindowInsetsControllerCompat(window, window.decorView).apply {
      isAppearanceLightStatusBars = false
      isAppearanceLightNavigationBars = false
    }
    val content = activity.findViewById<View>(android.R.id.content) ?: return
    ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
      val bars = insets.getInsets(
        WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
      )
      view.updatePadding(
        left = bars.left,
        top = bars.top,
        right = bars.right,
        bottom = bars.bottom
      )
      insets
    }
    content.requestApplyInsets()
  }

  /**
   * Pads a bottom-sheet dialog's content view by the bottom (navigation-bar) and IME insets, so
   * its primary CTA is not overlapped by the gesture/navigation bar. The sheet is a separate
   * window, so it is not covered by [apply] on the host Activity.
   */
  fun applyToBottomSheet(view: View) {
    ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
      val bottom = insets.getInsets(
        WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
      ).bottom
      v.updatePadding(bottom = bottom)
      insets
    }
    view.requestApplyInsets()
  }

  /**
   * True when the Activity's theme uses a translucent window or a transparent [windowBackground]
   * (e.g. the `Theme.AppCompat.Transparent.*` overlay themes). Such windows are intentionally
   * edge-to-edge and must not be padded or recolored.
   */
  private fun Activity.hasTransparentWindow(): Boolean {
    val attrs = intArrayOf(
      android.R.attr.windowIsTranslucent,
      android.R.attr.windowBackground
    )
    val typed = theme.obtainStyledAttributes(attrs)
    return try {
      val translucent = typed.getBoolean(0, false)
      val background = typed.getDrawable(1)
      translucent || (background is ColorDrawable && background.color == Color.TRANSPARENT)
    } finally {
      typed.recycle()
    }
  }
}
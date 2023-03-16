package com.appcoins.wallet.ui.common

import android.animation.LayoutTransition
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.InputType
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView

fun Drawable.toBitmap(): Bitmap {
  if (this is BitmapDrawable) {
    return this.bitmap
  }
  val bitmap =
    Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(bitmap)
  this.setBounds(0, 0, canvas.width, canvas.height)
  this.draw(canvas)
  return bitmap
}

fun Bitmap.mergeWith(centeredImage: Bitmap): Bitmap {

  val combined = Bitmap.createBitmap(width, height, config)
  val canvas = Canvas(combined)
  canvas.drawBitmap(this, Matrix(), null)

  val resizeLogo =
    Bitmap.createScaledBitmap(centeredImage, canvas.width / 5, canvas.height / 5, true)
  val centreX = (canvas.width - resizeLogo.width) / 2f
  val centreY = (canvas.height - resizeLogo.height) / 2f
  canvas.drawBitmap(resizeLogo, centreX, centreY, null)
  return combined
}

fun Int.convertDpToPx(resources: Resources): Int {
  return TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    resources.displayMetrics
  )
    .toInt()
}

/**
 * Executes block function with no layout transition animations. Note that it assumes that there is
 * no LayoutTransition.CHANGING, which is the case by default in "animateLayoutChanges".
 *
 * @see LayoutTransition.CHANGING
 */
inline fun View.withNoLayoutTransition(block: () -> Unit) {
  val lt = (this.parent as ViewGroup).layoutTransition
  lt.disableTransitionType(LayoutTransition.APPEARING)
  lt.disableTransitionType(LayoutTransition.CHANGE_APPEARING)
  lt.disableTransitionType(LayoutTransition.DISAPPEARING)
  lt.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)
  block()
  lt.enableTransitionType(LayoutTransition.APPEARING)
  lt.enableTransitionType(LayoutTransition.CHANGE_APPEARING)
  lt.enableTransitionType(LayoutTransition.DISAPPEARING)
  lt.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)
}

fun RecyclerView.addBottomItemDecoration(dimension: Float) {
  this.addItemDecoration(MarginItemDecoration(dimension.toInt()))
}

fun String.createColoredString(color: String): String {
  val parcedColor = when (color.length) {
    9 -> StringBuilder(color)
      .deleteCharAt(1)  // remove alpha channel
      .deleteCharAt(2)  // remove alpha channel
    7 -> color
    else -> "#ffffff"
  }
  return "<font color='${parcedColor}'>$this</font>"
}

fun TextView.setTextFromColored(coloredString: String) {
  this.text = HtmlCompat.fromHtml(coloredString, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

fun EditText.setReadOnly(value: Boolean, inputType: Int = InputType.TYPE_NULL) {
  isFocusable = !value
  isFocusableInTouchMode = !value
  this.inputType = inputType
}
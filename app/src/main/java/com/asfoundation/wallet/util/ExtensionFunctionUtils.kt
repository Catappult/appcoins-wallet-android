package com.asfoundation.wallet.util

import android.animation.LayoutTransition
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView
import com.asfoundation.wallet.ui.widget.MarginItemDecoration
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import retrofit2.HttpException
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * Class file to create kotlin extension functions
 *
 */

fun BigDecimal.scaleToString(scale: Int): String {
  val format = DecimalFormat("#.##")
  return format.format(this.setScale(scale, RoundingMode.FLOOR))
}

fun Throwable?.isNoNetworkException(): Boolean {
  return this != null && (this is IOException || this.cause != null && this.cause is IOException)
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

fun String.generateQrCode(windowManager: WindowManager, logo: Drawable): Bitmap {
  val size = Point()
  windowManager.defaultDisplay
      .getSize(size)
  val imageSize = (size.x * 0.9).toInt()
  val bitMatrix =
      MultiFormatWriter().encode(this, BarcodeFormat.QR_CODE, imageSize, imageSize,
          null)
  val barcodeEncoder = BarcodeEncoder()
  val qrCode = barcodeEncoder.createBitmap(bitMatrix)
  return qrCode.mergeWith(logo.toBitmap())
}

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

fun HttpException.getMessage(): String {
  val reader = this.response()
      ?.errorBody()
      ?.charStream()
  val message = reader?.readText()
  reader?.close()
  return if (message.isNullOrBlank()) message() else message
}

fun Locale.getLanguageAndCountryCodes(sep: String = "-"): String {
  return this.language + sep + this.country
}

inline fun <T> Iterable<T>.sumByBigDecimal(selector: (T) -> BigDecimal): BigDecimal {
  var sum = BigDecimal.ZERO
  for (element in this) {
    sum += selector(element)
  }
  return sum
}

fun Int.convertDpToPx(resources: Resources): Int {
  return TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      this.toFloat(),
      resources.displayMetrics
  )
      .toInt()
}

fun <T1 : Any, T2 : Any, R : Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
  return if (p1 != null && p2 != null) block(p1, p2) else null
}

fun <T1 : Any, T2 : Any, T3 : Any, R : Any> safeLet(p1: T1?, p2: T2?, p3: T3?,
                                                    block: (T1, T2, T3) -> R?): R? {
  return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
}

/**
 * Verifies and assigns the nullability of every input value. If at least one of the values is null,
 * it executes the closure function.
 */
inline fun <T : Any> guardLet(vararg elements: T?, closure: () -> Nothing): List<T> {
  return if (elements.all { it != null }) {
    elements.filterNotNull()
  } else {
    closure()
  }
}

/**
 * List by default can only destructure up to 5 components, this lets it go up to 6.
 */
operator fun <T> List<T>.component6() = get(5)

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

fun String.convertToBase64(): String {
  return Base64.encodeToString(this.toByteArray(), Base64.NO_WRAP)
}

fun String?.isEmailValid(): Boolean {
  return !this.isNullOrBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(this)
      .matches()
}

fun RecyclerView.addBottomItemDecoration(dimension: Float) {
  this.addItemDecoration(MarginItemDecoration(dimension.toInt()))
}

inline fun String.convertToDate(date: String): Date? {
  val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
  return dateFormat.parse(date)
}

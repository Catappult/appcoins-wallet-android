package com.appcoins.wallet.core.utils.android_common.extensions

import android.util.Base64
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
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

fun <T1 : Any, T2 : Any, R : Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
  return if (p1 != null && p2 != null) block(p1, p2) else null
}

fun <T1 : Any, T2 : Any, T3 : Any, R : Any> safeLet(
  p1: T1?, p2: T2?, p3: T3?,
  block: (T1, T2, T3) -> R?
): R? {
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

fun String.convertToBase64(): String {
  return Base64.encodeToString(this.toByteArray(), Base64.NO_WRAP)
}

fun String?.isEmailValid(): Boolean {
  return !this.isNullOrBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(this)
    .matches()
}

inline fun String.convertToDate(date: String): Date? {
  val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
  return dateFormat.parse(date)
}

inline fun Fragment.requestPermission(
  permission: String,
  crossinline granted: (permission: String) -> Unit = {},
  crossinline denied: (permission: String) -> Unit = {},
  crossinline explained: (permission: String) -> Unit = {}
) {
  registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
    when {
      result -> granted.invoke(permission)
      shouldShowRequestPermissionRationale(permission) -> denied.invoke(permission)
      else -> explained.invoke(permission)
    }
  }.launch(permission)
}
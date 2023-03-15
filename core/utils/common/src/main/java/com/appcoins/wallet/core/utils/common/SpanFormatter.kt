package com.appcoins.wallet.core.utils.common

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.SpannedString
import androidx.annotation.StringRes
import java.util.regex.Matcher
import java.util.regex.Pattern

private val FORMAT_SEQUENCE: Pattern =
  Pattern.compile("%([0-9]+\\$|<?)([^a-zA-z%]*)([[a-zA-Z%]&&[^tT]]|[tT][a-zA-Z])")

/**
 * Extension to format a string with a spannable
 * TODO: Refactor this maybe? This was copied from somewhere with automatic conversion to Kotlin
 */
fun Context.getStringSpanned(@StringRes resId: Int, vararg args: Any): Spanned {
  val out = SpannableStringBuilder(getString(resId))
  var i = 0
  var argAt = -1
  while (i < out.length) {
    val m: Matcher = FORMAT_SEQUENCE.matcher(out)
    if (!m.find(i)) break
    i = m.start()
    val exprEnd: Int = m.end()
    val argTerm: String = m.group(1)!!
    val modTerm: String = m.group(2)!!
    val typeTerm: String = m.group(3)!!
    var cookedArg: CharSequence
    when (typeTerm) {
      "%" -> {
        cookedArg = "%"
      }
      "n" -> {
        cookedArg = "\n"
      }
      else -> {
        var argIdx = 0
        argIdx = when (argTerm) {
          "" -> ++argAt
          "<" -> argAt
          else -> argTerm.substring(
            0,
            argTerm.length - 1
          ).toInt() - 1
        }
        val argItem = args[argIdx]
        cookedArg = if (typeTerm == "s" && argItem is Spanned) {
          argItem
        } else {
          String.format("%$modTerm$typeTerm", argItem)
        }
      }
    }
    out.replace(i, exprEnd, cookedArg)
    i += cookedArg.length
  }
  return SpannedString(out)
}
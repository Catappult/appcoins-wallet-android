package com.appcoins.wallet.ui.widgets.component

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class CurrencyVisualTransformation(
  private val currencySymbol: String
) : VisualTransformation {

  override fun filter(text: AnnotatedString): TransformedText {
    val transformedText = if (text.text.isEmpty()) {
      AnnotatedString("")
    } else {
      AnnotatedString(currencySymbol + text.text)
    }
    val currencyOffset = if (text.text.isEmpty()) {
      0
    } else {
      currencySymbol.length
    }
    val offsetMapping = object : OffsetMapping {
      override fun originalToTransformed(offset: Int): Int {
        return offset + currencyOffset
      }
      override fun transformedToOriginal(offset: Int): Int {
        val shifted = offset - currencyOffset
        return if (shifted >= 0) shifted else 0
      }
    }
    return TransformedText(transformedText, offsetMapping)
  }
}
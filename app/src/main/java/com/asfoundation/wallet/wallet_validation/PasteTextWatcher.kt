package com.asfoundation.wallet.wallet_validation

import android.content.ClipDescription
import android.content.ClipboardManager
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import org.apache.commons.lang3.StringUtils

class PasteTextWatcher(
    private val inputTexts: Array<EditText>,
    private val clipboardManager: ClipboardManager,
    private val selectedPosition: Int
) : TextWatcher {

  private var isPaste = false
  private var isStart = false
  private var isDelete = false
  private var previousChar = ""

  override fun afterTextChanged(s: Editable?) {
    if (isDelete) {
      if (selectedPosition > 0) {
        inputTexts[selectedPosition - 1].requestFocus()
        inputTexts[selectedPosition - 1].setSelection(inputTexts[selectedPosition - 1].length())
        return
      }
    }

    if (s?.length ?: 0 > 1 && isPaste && isValidPaste()) {
      inputTexts[selectedPosition].setText(previousChar)
      val text = getTextFromClipboard()
      text?.forEachIndexed { index, digit ->
        when (index) {
          0, 1, 2, 3, 4, 5 -> inputTexts[index].setText(digit.toString())
          else -> return@forEachIndexed
        }
      }
    }
    if (s?.length ?: 0 > 1) {
      if (isStart) {
        s?.delete(1, 2)
      } else {
        s?.delete(0, 1)
      }
    }

  }

  override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    isStart = start == 0
    isDelete = (start == 0 && count == 1 && after == 0 && s?.length ?: 0 <= 1)
    if (after > 0) {
      previousChar = s.toString()
    }
  }

  override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    isPaste = count > 1
  }

  private fun isValidPaste(): Boolean {
    return clipboardManager.primaryClipDescription?.hasMimeType(
        ClipDescription.MIMETYPE_TEXT_PLAIN) == true && StringUtils.isNumeric(
        getTextFromClipboard())
  }

  private fun getTextFromClipboard(): String? {
    return clipboardManager.primaryClip?.getItemAt(0)
        ?.text?.toString()
        ?.replace(Regex("[^\\d.]"), "")
  }

}
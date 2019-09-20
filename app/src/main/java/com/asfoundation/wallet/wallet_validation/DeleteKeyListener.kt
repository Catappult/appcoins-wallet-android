package com.asfoundation.wallet.wallet_validation

import android.view.KeyEvent
import android.view.View
import android.widget.EditText

class DeleteKeyListener(
    private val inputTexts: Array<EditText>,
    private val selectedPosition: Int
) : View.OnKeyListener {

  override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
    if (event?.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
      inputTexts[selectedPosition].setText("")
      if (selectedPosition > 0) {
        inputTexts[selectedPosition - 1].requestFocus()
      }
      return true
    }
    return false
  }

}
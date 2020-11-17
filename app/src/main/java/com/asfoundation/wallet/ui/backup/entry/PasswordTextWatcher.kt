package com.asfoundation.wallet.ui.backup.entry

import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class PasswordTextWatcher(private val button: Button,
                          private val errorPasswordInputLayout: TextInputLayout,
                          private val otherPassword: TextInputEditText) : TextWatcher {

  override fun afterTextChanged(s: Editable?) {
    val passwordText = s.toString()
    val otherPasswordText = otherPassword.text.toString()
    if (passwordText == otherPasswordText) {
      if (passwordText.isNotEmpty() && otherPasswordText.isNotEmpty()) {
        button.isEnabled = true
        errorPasswordInputLayout.error = null
      } else {
        button.isEnabled = false
        errorPasswordInputLayout.error = null
      }
    } else {
      button.isEnabled = false
      if (passwordText.isNotEmpty() && otherPasswordText.isNotEmpty()) {
        errorPasswordInputLayout.error = "Password don't match"
      } else {
        errorPasswordInputLayout.error = null
      }
    }
  }

  override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
  override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

}
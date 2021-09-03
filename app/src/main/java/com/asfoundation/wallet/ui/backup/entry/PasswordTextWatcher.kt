package com.asfoundation.wallet.ui.backup.entry

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText
import io.reactivex.subjects.PublishSubject

class PasswordTextWatcher(private val passwordSubject: PublishSubject<PasswordFields>,
                          private val otherPassword: TextInputEditText) : TextWatcher {

  override fun afterTextChanged(s: Editable?) {
    val passwordText = s.toString()
    val otherPasswordText = otherPassword.text.toString()
    val passwordFields = checkPasswordFields(passwordText, otherPasswordText)
    passwordSubject.onNext(passwordFields)
  }

  override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
  override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

  private fun checkPasswordFields(passwordText: String, otherPasswordText: String): PasswordFields {
    return if (passwordText == otherPasswordText) {
      if (passwordText.isNotEmpty() && otherPasswordText.isNotEmpty()) {
        PasswordFields(match = true, empty = false)
      } else {
        PasswordFields(match = true, empty = true)
      }
    } else {
      if (passwordText.isNotEmpty() && otherPasswordText.isNotEmpty()) {
        PasswordFields(match = false, empty = false)
      } else {
        PasswordFields(match = false, empty = true)
      }
    }
  }
}
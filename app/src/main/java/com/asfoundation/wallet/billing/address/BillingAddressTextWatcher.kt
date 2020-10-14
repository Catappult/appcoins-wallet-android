package com.asfoundation.wallet.billing.address

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputLayout

class BillingAddressTextWatcher(private val view: TextInputLayout) : TextWatcher {

  override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

  override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

  override fun afterTextChanged(s: Editable) {
    if (!TextUtils.isEmpty(s)) view.error = null
  }

}
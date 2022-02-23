package com.asfoundation.wallet.restore.password

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.res.ResourcesCompat
import com.asf.wallet.R
import com.google.android.material.textfield.TextInputLayout

class PasswordInputLayout : TextInputLayout {
  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context,
      attrs, defStyleAttr)

  override fun setError(errorText: CharSequence?) {
    super.setError(errorText)
    if (errorText != null) {
      this.editText?.setTextColor(
          ResourcesCompat.getColor(resources, R.color.colorPrimaryDark, null))
    } else {
      this.editText?.setTextColor(
          ResourcesCompat.getColor(resources, R.color.black, null))
    }
  }
}
package com.appcoins.wallet.ui.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.appcoins.wallet.ui.common.convertDpToPx
import com.appcoins.wallet.ui.common.setReadOnly
import com.appcoins.wallet.ui.widgets.databinding.LayoutWalletTextFieldViewBinding
import com.google.android.material.textfield.TextInputLayout.END_ICON_NONE

class WalletTextFieldView : FrameLayout {

  private val views =
    LayoutWalletTextFieldViewBinding.inflate(LayoutInflater.from(context), this, true)

  private var type = Type.FILLED

  private var color = ContextCompat.getColor(this.context, R.color.styleguide_dark_grey)

  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
  ) : super(context, attrs, defStyleAttr) {
    retrievePreferences(attrs, defStyleAttr)
  }

  private fun retrievePreferences(attrs: AttributeSet?, defStyleAttr: Int) {
    val typedArray =
      context.obtainStyledAttributes(attrs, R.styleable.WalletTextFieldView, defStyleAttr, 0)
    val type = Type.values()[typedArray.getInt(R.styleable.WalletTextFieldView_textFieldType, 0)]
    setType(type)
    val hint = typedArray.getString(R.styleable.WalletTextFieldView_textFieldHint) ?: ""
    setHint(hint)
    val textFieldColor = typedArray.getColor(R.styleable.WalletTextFieldView_textFieldColor, color)
    setColor(textFieldColor)
    typedArray.recycle()
  }

  fun setType(type: Type) {
    this.type = type
    applyType()
  }

  fun setHint(text: CharSequence) {
    views.textInputEditText.hint = text
  }

  fun setText(text: CharSequence) {
    views.textInputEditText.setText(text)
  }

  fun getText(): String {
    return views.textInputEditText.text.toString()
  }

  fun setColor(@ColorInt color: Int) {
    this.color = color
    applyType()
  }

  fun setError(errorText: CharSequence?) {
    views.textInputLayout.error = errorText
  }

  private fun applyType() {
    when (type) {
      Type.FILLED -> {
        views.textInputEditText.setReadOnly(value = false, inputType = InputType.TYPE_CLASS_TEXT)
        views.textInputLayout.boxBackgroundColor = color
        views.textInputLayout.boxStrokeColor =
          ContextCompat.getColor(this.context, R.color.transparent)
        views.textInputLayout.boxStrokeWidth = 0
        views.textInputLayout.endIconMode = END_ICON_NONE
        views.textInputLayout.editText?.setTextColor(resources.getColor(R.color.styleguide_white))
        views.textInputLayout.editText?.setHintTextColor(
          resources.getColor(R.color.styleguide_dark_grey)
        )
      }
      Type.OUTLINED -> {
        views.textInputEditText.setReadOnly(value = false, inputType = InputType.TYPE_CLASS_TEXT)
        views.textInputLayout.boxBackgroundColor =
          ContextCompat.getColor(this.context, R.color.styleguide_blue_secondary)
        views.textInputLayout.boxStrokeColor =
          ContextCompat.getColor(this.context, R.color.transparent)
        views.textInputLayout.endIconMode = END_ICON_NONE
      }
      Type.PASSWORD -> {
        views.textInputEditText.setReadOnly(
          value = false, inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        )
        views.textInputLayout.boxBackgroundColor = color
        views.textInputLayout.boxStrokeColor =
          ContextCompat.getColor(this.context, R.color.transparent)
        views.textInputLayout.boxStrokeWidth = 0
        views.textInputLayout.isPasswordVisibilityToggleEnabled = true
        views.textInputLayout.setEndIconTintList(ColorStateList.valueOf(resources.getColor(R.color.styleguide_white)))
        views.textInputLayout.editText?.setTextColor(resources.getColor(R.color.styleguide_white))
        views.textInputLayout.editText?.setHintTextColor(resources.getColor(R.color.styleguide_dark_grey))
      }
      Type.NUMBER -> {
        views.textInputEditText.inputType =
          InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        views.textInputLayout.boxBackgroundColor = color
        views.textInputLayout.boxStrokeColor =
          ContextCompat.getColor(this.context, R.color.transparent)
        views.textInputLayout.boxStrokeWidth = 0
        views.textInputLayout.endIconMode = END_ICON_NONE
        views.textInputLayout.editText?.setTextColor(resources.getColor(R.color.styleguide_white))
        views.textInputLayout.editText?.setHintTextColor(
          resources.getColor(R.color.styleguide_dark_grey)
        )
      }
      Type.READ_ONLY -> {
        views.textInputEditText.setReadOnly(value = true)
        views.textInputLayout.boxBackgroundColor =
          ContextCompat.getColor(this.context, R.color.styleguide_blue)
        views.textInputLayout.editText?.setTextColor(resources.getColor(R.color.styleguide_white))
        views.textInputLayout.boxStrokeColor = color
        views.textInputLayout.boxStrokeWidth = 1.convertDpToPx(resources)
      }
    }
  }

  fun addTextChangedListener(watcher: TextWatcher) {
    return views.textInputEditText.addTextChangedListener(watcher)
  }

  enum class Type {
    FILLED,
    OUTLINED,
    PASSWORD,
    READ_ONLY,
    NUMBER
  }
}

package com.asfoundation.wallet.ui.common

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.asf.wallet.R
import com.asf.wallet.databinding.LayoutWalletTextFieldViewBinding
import com.asfoundation.wallet.util.convertDpToPx

class WalletTextFieldView : FrameLayout {

  private val views =
      LayoutWalletTextFieldViewBinding.inflate(LayoutInflater.from(context), this, true)

  private var type = Type.FILLED

  private var color = ContextCompat.getColor(this.context, R.color.grey_70)

  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
      context, attrs,
      defStyleAttr
  ) {
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
    views.textInput.hint = text
  }

  fun setText(text: CharSequence) {
    views.textInput.setText(text)
  }

  fun getText(): String {
    return views.textInput.text.toString()
  }

  fun setColor(@ColorInt color: Int) {
    this.color = color
    applyType()
  }

  private fun applyType() {
    when (type) {
      Type.FILLED -> {
        views.root.setCardBackgroundColor(color)
        views.root.strokeColor = ContextCompat.getColor(this.context, R.color.transparent)
        views.root.strokeWidth = 0
        views.root.setRippleColorResource(R.color.white)
      }
      Type.OUTLINED -> {
        views.root.setCardBackgroundColor(ContextCompat.getColor(this.context, R.color.transparent))
        views.root.strokeColor = color
        views.root.strokeWidth = 1.convertDpToPx(resources)
        views.root.rippleColor = ColorStateList.valueOf(color)
      }
    }
  }

  override fun setOnClickListener(l: OnClickListener?) {
    views.root.setOnClickListener(l)
  }

  enum class Type { FILLED, OUTLINED }
}
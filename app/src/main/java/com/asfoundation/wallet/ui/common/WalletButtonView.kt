package com.asfoundation.wallet.ui.common

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.asf.wallet.R
import com.asf.wallet.databinding.LayoutWalletButtonViewBinding
import com.asfoundation.wallet.util.convertDpToPx

class WalletButtonView : FrameLayout {

  private val views =
      LayoutWalletButtonViewBinding.inflate(LayoutInflater.from(context), this, true)

  private var type = Type.FILLED

  private var color = ContextCompat.getColor(this.context, R.color.wild_watermelon)

  private var enabled = true

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
        context.obtainStyledAttributes(attrs, R.styleable.WalletButtonView, defStyleAttr, 0)
    val type = Type.values()[typedArray.getInt(R.styleable.WalletButtonView_buttonType, 0)]
    setType(type)
    val string = typedArray.getString(R.styleable.WalletButtonView_buttonText) ?: ""
    setText(string)
    val buttonColor = typedArray.getColor(R.styleable.WalletButtonView_buttonColor, color)
    setColor(buttonColor)
    typedArray.recycle()
  }

  fun setType(type: Type) {
    this.type = type
    applyType()
  }

  fun setText(text: CharSequence) {
    views.text.text = text
  }

  fun setColor(@ColorInt color: Int) {
    this.color = color
    applyType()
  }

  override fun setEnabled(enabled: Boolean) {
    this.enabled = enabled
    applyType()
  }

  fun setColorResource(@ColorRes colorRes: Int) {
    setColor(ContextCompat.getColor(this.context, colorRes))
  }

  private fun applyType() {
    if (enabled) {
      views.root.isClickable = true

      when (type) {
        Type.FILLED -> {
          views.root.setCardBackgroundColor(color)
          views.root.strokeColor = ContextCompat.getColor(this.context, R.color.transparent)
          views.root.strokeWidth = 0
          views.root.setRippleColorResource(R.color.white)
          views.text.setTextColor(ContextCompat.getColor(this.context, R.color.white))
        }
        Type.OUTLINED -> {
          views.root.setCardBackgroundColor(
              ContextCompat.getColor(this.context, R.color.transparent))
          views.root.strokeColor = color
          views.root.strokeWidth = 1.convertDpToPx(resources)
          views.root.rippleColor = ColorStateList.valueOf(color)
          views.text.setTextColor(color)
        }
        Type.TEXT -> {
          views.root.setCardBackgroundColor(
              ContextCompat.getColor(this.context, R.color.transparent))
          views.root.strokeColor = ContextCompat.getColor(this.context, R.color.transparent)
          views.root.strokeWidth = 0
          views.root.setRippleColorResource(R.color.transparent)
          views.text.setTextColor(color)
        }
      }
    } else {
      views.root.isClickable = false
      views.root.setCardBackgroundColor(ContextCompat.getColor(this.context, R.color.grey_c9))
      views.root.strokeColor = ContextCompat.getColor(this.context, R.color.transparent)
      views.root.strokeWidth = 0
      views.root.setRippleColorResource(R.color.white)
      views.text.setTextColor(ContextCompat.getColor(this.context, R.color.white))
    }
  }

  override fun setOnClickListener(l: OnClickListener?) {
    views.root.setOnClickListener(l)
  }

  enum class Type { FILLED, OUTLINED, TEXT }
}
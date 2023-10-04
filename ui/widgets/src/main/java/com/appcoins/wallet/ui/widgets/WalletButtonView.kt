package com.appcoins.wallet.ui.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.appcoins.wallet.ui.widgets.databinding.LayoutWalletButtonViewBinding
import com.appcoins.wallet.ui.common.convertDpToPx
import com.appcoins.wallet.ui.common.setMargins

class WalletButtonView : FrameLayout {

  private val views =
    LayoutWalletButtonViewBinding.inflate(LayoutInflater.from(context), this, true)

  private var type = Type.FILLED

  private var color = ContextCompat.getColor(this.context, R.color.styleguide_pink)

  private var greyColor = ContextCompat.getColor(this.context, R.color.styleguide_light_grey)

  private var enabled = true

  private var isAllCaps = false

  private var imageRight: Drawable? = null

  private var imageLeft: Drawable? = null

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
    val caps = typedArray.getBoolean(R.styleable.WalletButtonView_buttonIsAllCaps, false)
    setIsAllCaps(caps)
    val imgLeft = typedArray.getDrawable(R.styleable.WalletButtonView_buttonImageLeft)
    setImageLeft(imgLeft)
    val imgRight = typedArray.getDrawable(R.styleable.WalletButtonView_buttonImageRight)
    setImageRight(imgRight)
    typedArray.recycle()
  }

  fun setType(type: Type) {
    this.type = type
    applyType()
  }

  fun setText(text: CharSequence) {
    views.text.text = text
  }

  fun setTextRes(@StringRes textRes: Int) {
    views.text.text = context.getString(textRes)
  }

  fun setColor(@ColorInt color: Int) {
    this.color = color
    applyType()
  }

  override fun setEnabled(enabled: Boolean) {
    this.enabled = enabled
    applyType()
  }

  fun setIsAllCaps(enabled: Boolean) {
    this.isAllCaps = enabled
    applyType()
  }

  fun setImageLeft(image: Drawable?) {
    this.imageLeft = image
    applyType()
  }

  fun setImageRight(image: Drawable?) {
    this.imageRight = image
    applyType()
  }

  private fun applyType() {
    if (enabled) {
      views.root.isClickable = true

      when (type) {
        Type.FILLED -> {
          views.root.setCardBackgroundColor(color)
          views.root.strokeColor = ContextCompat.getColor(this.context, R.color.transparent)
          views.root.strokeWidth = 0
          views.root.setRippleColorResource(R.color.styleguide_white)
          views.text.setTextColor(ContextCompat.getColor(this.context, R.color.styleguide_white))
          views.text.isAllCaps = isAllCaps
          imageLeft?.let { image ->
            views.imageLeft.setImageDrawable(image)
            views.imageLeft.visibility = View.VISIBLE
            views.imageLeft.imageTintList =
              ColorStateList.valueOf(ContextCompat.getColor(this.context, R.color.styleguide_white))
          }
          imageRight?.let { image ->
            views.imageRight.setImageDrawable(image)
            views.imageRight.visibility = View.VISIBLE
            views.imageRight.imageTintList =
              ColorStateList.valueOf(ContextCompat.getColor(this.context, R.color.styleguide_white))
          }
        }
        Type.OUTLINED -> {
          views.root.setCardBackgroundColor(
            ContextCompat.getColor(this.context, R.color.transparent)
          )
          views.root.strokeColor = color
          views.root.strokeWidth = 1.convertDpToPx(resources)
          views.root.rippleColor = ColorStateList.valueOf(color)
          views.text.setTextColor(color)
          views.text.isAllCaps = isAllCaps
          imageLeft?.let { image ->
            views.imageLeft.setImageDrawable(image)
            views.imageLeft.visibility = View.VISIBLE
            views.imageLeft.imageTintList = ColorStateList.valueOf(color)
          }
          imageRight?.let { image ->
            views.imageRight.setImageDrawable(image)
            views.imageRight.visibility = View.VISIBLE
            views.imageRight.imageTintList = ColorStateList.valueOf(color)
          }
        }

        Type.OUTLINED_GREY -> {
          views.root.setCardBackgroundColor(
            ContextCompat.getColor(this.context, R.color.transparent)
          )
          views.root.strokeColor = greyColor
          views.root.strokeWidth = 1.convertDpToPx(resources)
          views.root.rippleColor = ColorStateList.valueOf(color)
          views.text.setTextColor(greyColor)
          views.text.isAllCaps = isAllCaps
          imageLeft?.let { image ->
            views.imageLeft.setImageDrawable(image)
            views.imageLeft.visibility = View.VISIBLE
            views.imageLeft.imageTintList = ColorStateList.valueOf(color)
          }
          imageRight?.let { image ->
            views.imageRight.setImageDrawable(image)
            views.imageRight.visibility = View.VISIBLE
            views.imageRight.imageTintList = ColorStateList.valueOf(color)
          }
        }

        Type.TEXT -> {
          views.root.setCardBackgroundColor(
            ContextCompat.getColor(this.context, R.color.transparent)
          )
          views.root.strokeColor = ContextCompat.getColor(this.context, R.color.transparent)
          views.root.strokeWidth = 0
          views.root.setRippleColorResource(R.color.styleguide_medium_grey)
          views.text.setTextColor(color)
          views.text.isAllCaps = isAllCaps
          imageLeft?.let { image ->
            views.imageLeft.setImageDrawable(image)
            views.imageLeft.visibility = View.VISIBLE
            views.imageLeft.imageTintList = ColorStateList.valueOf(color)
          }
          imageRight?.let { image ->
            views.text.setMargins(16, 0, 8, 0)
            views.text.setPadding(0, 0, 0, 0)
            views.imageRight.setImageDrawable(image)
            views.imageRight.visibility = View.VISIBLE
            views.imageRight.imageTintList = ColorStateList.valueOf(color)
          }
        }
        Type.FILLED_GRAY_PINK -> {
          views.root.setCardBackgroundColor(
            ContextCompat.getColor(
              this.context,
              R.color.styleguide_white_transparent_20
            )
          )
          views.root.strokeColor = ContextCompat.getColor(this.context, R.color.transparent)
          views.root.strokeWidth = 0
          views.root.setRippleColorResource(R.color.styleguide_white)
          views.text.setTextColor(ContextCompat.getColor(this.context, R.color.styleguide_white))
          views.text.isAllCaps = isAllCaps
          views.text.setPadding(0, 0, 0, 0)
          views.text.setMargins(16, 0, 8, 0)
          imageLeft?.let { image ->
            views.imageLeft.setImageDrawable(image)
            views.imageLeft.visibility = View.VISIBLE
            views.imageLeft.imageTintList =
              ColorStateList.valueOf(ContextCompat.getColor(this.context, R.color.styleguide_pink))
            views.imageLeft.setMargins(0, 0, 0, 0)
          }
          imageRight?.let { image ->
            views.imageRight.setImageDrawable(image)
            views.imageRight.visibility = View.VISIBLE
            views.imageRight.imageTintList =
              ColorStateList.valueOf(ContextCompat.getColor(this.context, R.color.styleguide_pink))
            views.imageRight.setMargins(0, 0, 0, 0)
          }
        }
        else -> {}
      }
    } else {
      views.root.isClickable = false
      views.root.setCardBackgroundColor(ContextCompat.getColor(this.context, R.color.styleguide_medium_grey))
      views.root.strokeColor = ContextCompat.getColor(this.context, R.color.transparent)
      views.root.strokeWidth = 0
      views.root.setRippleColorResource(R.color.styleguide_white)
      views.text.setTextColor(ContextCompat.getColor(this.context, R.color.styleguide_white))
    }
  }

  override fun setOnClickListener(l: OnClickListener?) {
    views.root.setOnClickListener(l)
  }

  enum class Type { FILLED, OUTLINED, TEXT, DISABLE, FILLED_GRAY_PINK, OUTLINED_GREY }
}
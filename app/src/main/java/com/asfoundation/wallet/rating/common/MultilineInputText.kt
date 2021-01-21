package com.asfoundation.wallet.rating.common

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.asf.wallet.R
import kotlinx.android.synthetic.main.multiline_input_layout.view.*

class MultilineInputText : FrameLayout {

  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
      defStyleAttr) {
    inflate(context, R.layout.multiline_input_layout, this)
  }

  fun getText(): String {
    return input_text.text.toString()
  }

  fun setError(errorText: String) {
    error_text.text = errorText
    root_view.setBackgroundResource(R.drawable.rectangle_outline_red_radius_8dp)
    error_layout.visibility = View.VISIBLE
  }

  fun reset() {
    root_view.setBackgroundResource(R.drawable.rectangle_outline_grey_radius_8dp)
    error_layout.visibility = View.GONE
  }

  fun addTextWatcher(textWatcher: TextWatcher) {
    input_text.addTextChangedListener(textWatcher)
  }

}
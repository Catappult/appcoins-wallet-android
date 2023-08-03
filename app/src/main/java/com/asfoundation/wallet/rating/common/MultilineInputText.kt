package com.asfoundation.wallet.rating.common

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.asf.wallet.R
import com.google.android.material.textfield.TextInputEditText

class MultilineInputText : FrameLayout {

  private val input_text : TextInputEditText
  private val error_text : TextView
  private val root_view : ConstraintLayout
  private val error_layout : LinearLayout

  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
      defStyleAttr) {
    inflate(context, R.layout.multiline_input_layout, this)

    input_text = findViewById(R.id.input_text)
    error_text = findViewById(R.id.error_text)
    root_view = findViewById(R.id.root_view)
    error_layout = findViewById(R.id.error_layout)
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
    root_view.setBackgroundResource(R.drawable.rectangle_outline_blue_radius_8dp)
    error_layout.visibility = View.GONE
  }

  fun addTextWatcher(textWatcher: TextWatcher) {
    input_text.addTextChangedListener(textWatcher)
  }

}
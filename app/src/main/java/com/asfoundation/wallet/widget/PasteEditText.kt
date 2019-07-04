package com.asfoundation.wallet.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText


class PasteEditText(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int,
                    defStyleRes: Int)
  : EditText(context, attributeSet, defStyleAttr, defStyleRes) {

  interface OnPasteListener {
    fun onPaste()
  }

  constructor(context: Context) : this(context, null, 0, 0)
  constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet,
      android.R.attr.editTextStyle, 0)

  constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int)
      : this(context, attributeSet, defStyleAttr, 0)


  private var mOnPasteListener: OnPasteListener? = null


  fun setOnPasteListener(listener: OnPasteListener) {
    mOnPasteListener = listener
  }

  override fun onTextContextMenuItem(id: Int): Boolean {
    return when (id) {
      android.R.id.paste -> {
        onPaste()
        true
      }
      else -> super.onTextContextMenuItem(id)
    }
  }

  private fun onPaste() {
    mOnPasteListener?.onPaste()
  }
}
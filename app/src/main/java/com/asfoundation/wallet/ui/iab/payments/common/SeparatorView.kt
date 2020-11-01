package com.asfoundation.wallet.ui.iab.payments.common

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.Dimension
import androidx.annotation.Px
import com.asf.wallet.R

/**
 * For now this only supports horizontal separators, but could totally support vertical
 * in the future.
 */
class SeparatorView : View {

  private var type: Type = Type.SOLID

  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
      defStyleAttr) {
    setLayerType(LAYER_TYPE_SOFTWARE, null)
    retrievePreferences(attrs, defStyleAttr)
  }

  private fun retrievePreferences(attrs: AttributeSet?, defStyleAttr: Int) {
    val typedArray =
        context.obtainStyledAttributes(attrs, R.styleable.SeparatorView, defStyleAttr, 0)
    setType(Type.values()[typedArray.getInt(R.styleable.SeparatorView_line_type, 0)])
    typedArray.recycle()
  }

  fun setType(type: Type) {
    this.type = type
    when (type) {
      Type.SOLID -> {
        setBackgroundResource(R.color.layout_separator_color)
      }
      Type.DASHED -> {
        setBackgroundResource(R.drawable.dashed_line)
      }
    }
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val heightSpec = if (type == Type.DASHED) {
      MeasureSpec.makeMeasureSpec(dpToPx(5).toInt(), MeasureSpec.EXACTLY)
    } else {
      MeasureSpec.makeMeasureSpec(dpToPx(1).toInt(), MeasureSpec.EXACTLY)
    }
    super.onMeasure(widthMeasureSpec, heightSpec);
  }

  enum class Type { SOLID, DASHED }

  @Px
  private fun dpToPx(@Dimension(unit = Dimension.DP) dp: Int): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
        resources.displayMetrics)
  }
}
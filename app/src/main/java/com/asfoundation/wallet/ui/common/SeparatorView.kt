package com.asfoundation.wallet.ui.common

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.Dimension
import androidx.annotation.Px
import com.asf.wallet.R

class SeparatorView : View {

  private var type: Type = Type.SOLID
  private var orientation: Orientation = Orientation.HORIZONTAL

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
    this.type = Type.values()[typedArray.getInt(R.styleable.SeparatorView_line_type, 0)]
    this.orientation =
        Orientation.values()[typedArray.getInt(R.styleable.SeparatorView_line_orientation, 0)]
    applyOrientationType()
    typedArray.recycle()
  }

  fun setOrientation(orientation: Orientation) {
    this.orientation = orientation
    applyOrientationType()
  }

  fun setType(type: Type) {
    this.type = type
    applyOrientationType()
  }

  private fun applyOrientationType() {
    when (type) {
      Type.SOLID -> {
        setBackgroundResource(R.color.layout_separator_color)
      }
      Type.DASHED -> {
        if (orientation == Orientation.VERTICAL) {
          setBackgroundResource(R.drawable.dashed_vertical_line)
        } else {
          setBackgroundResource(R.drawable.dashed_line)
        }
      }
    }
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    var widthSpec = widthMeasureSpec
    var heightSpec = heightMeasureSpec

    when (orientation) {
      Orientation.HORIZONTAL -> {
        heightSpec = if (type == Type.DASHED) {
          MeasureSpec.makeMeasureSpec(dpToPx(5).toInt(), MeasureSpec.EXACTLY)
        } else {
          MeasureSpec.makeMeasureSpec(dpToPx(1).toInt(), MeasureSpec.EXACTLY)
        }
      }
      Orientation.VERTICAL -> {
        widthSpec = MeasureSpec.makeMeasureSpec(dpToPx(1).toInt(), MeasureSpec.EXACTLY)
      }
    }

    super.onMeasure(widthSpec, heightSpec);
  }

  enum class Type { SOLID, DASHED }

  enum class Orientation { HORIZONTAL, VERTICAL }

  @Px
  private fun dpToPx(@Dimension(unit = Dimension.DP) dp: Int): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
        resources.displayMetrics)
  }
}
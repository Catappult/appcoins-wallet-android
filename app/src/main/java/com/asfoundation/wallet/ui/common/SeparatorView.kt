package com.asfoundation.wallet.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.asf.wallet.R
import com.asfoundation.wallet.util.convertDpToPx

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
        setBackgroundResource(R.color.styleguide_light_grey)
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
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
          heightSpec = if (type == Type.DASHED) {
            MeasureSpec.makeMeasureSpec(5.convertDpToPx(resources), MeasureSpec.EXACTLY)
          } else {
            MeasureSpec.makeMeasureSpec(1.convertDpToPx(resources), MeasureSpec.EXACTLY)
          }
        }
      }
      Orientation.VERTICAL -> {
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
          widthSpec = MeasureSpec.makeMeasureSpec(1.convertDpToPx(resources), MeasureSpec.EXACTLY)
        }
      }
    }

    super.onMeasure(widthSpec, heightSpec);
  }

  enum class Type { SOLID, DASHED }

  enum class Orientation { HORIZONTAL, VERTICAL }

}
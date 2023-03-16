package com.appcoins.wallet.ui.widgets

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.widgets.databinding.LayoutToolbarTextSwitcherBinding
import kotlinx.parcelize.Parcelize

class AnimatedTextSwitcher : FrameLayout {
  @Parcelize
  class ToolbarTextSwitcherState(val superSavedState: Parcelable?, val animateFirstView: Boolean,
                                 val text: CharSequence) : View.BaseSavedState(superSavedState),
      Parcelable

  private val views =
      LayoutToolbarTextSwitcherBinding.inflate(LayoutInflater.from(context), this, true)

  private var animateFirstView = true
  private var text: CharSequence = ""

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
        context.obtainStyledAttributes(attrs, R.styleable.AnimatedTextSwitcher, defStyleAttr, 0)
    val string = typedArray.getString(R.styleable.AnimatedTextSwitcher_toolbarText) ?: ""
    text = string
    views.textSwitcher.setCurrentText(string)
    typedArray.recycle()
  }

  override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
    dispatchFreezeSelfOnly(container)
  }

  override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
    dispatchThawSelfOnly(container)
  }

  override fun onSaveInstanceState(): Parcelable {
    return ToolbarTextSwitcherState(super.onSaveInstanceState(), animateFirstView, text)
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    val toolbarState = state as? ToolbarTextSwitcherState
    super.onRestoreInstanceState(toolbarState?.superSavedState ?: state)
    animateFirstView = toolbarState?.animateFirstView ?: true
    text = toolbarState?.text ?: ""
    views.textSwitcher.setCurrentText(text)
  }

  fun setText(str: CharSequence) {
    if (str == getText()) return
    if (animateFirstView) {
      views.textSwitcher.setInAnimation(context, R.anim.slide_in_down)
      views.textSwitcher.setOutAnimation(context, R.anim.slide_out_up)
    } else {
      views.textSwitcher.setInAnimation(context, R.anim.slide_in_down)
      views.textSwitcher.setOutAnimation(context, R.anim.slide_out_up)
    }
    views.textSwitcher.setText(str)
    text = str
    animateFirstView = !animateFirstView
  }

  fun getText(): String {
    return (views.textSwitcher.currentView as TextView).text.toString()
  }
}
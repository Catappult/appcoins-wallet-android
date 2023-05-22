package com.appcoins.wallet.ui.widgets

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.appcoins.wallet.ui.common.R
import com.google.android.material.snackbar.Snackbar

class SystemView : FrameLayout, View.OnClickListener {
  private var progress: ProgressBar? = null
  private var errorBox: View? = null
  private var messageTxt: TextView? = null
  private var tryAgain: View? = null
  private var onTryAgainClickListener: OnClickListener? = null
  private var emptyBox: FrameLayout? = null
  private var swipeRefreshLayout: SwipeRefreshLayout? = null
  private var recyclerView: RecyclerView? = null

  constructor(context: Context) : super(context) {}
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  ) {
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    val view = LayoutInflater.from(context)
      .inflate(com.appcoins.wallet.ui.widgets.R.layout.layout_system_view, this, false)
    addView(view)
    progress = view.findViewById(com.appcoins.wallet.ui.widgets.R.id.progress)
    errorBox = view.findViewById(com.appcoins.wallet.ui.widgets.R.id.error_box)
    messageTxt = view.findViewById(com.appcoins.wallet.ui.widgets.R.id.message)
    tryAgain = view.findViewById(com.appcoins.wallet.ui.widgets.R.id.try_again)
    tryAgain?.setOnClickListener(this)
    emptyBox = view.findViewById(com.appcoins.wallet.ui.widgets.R.id.empty_box)
  }

  fun attachSwipeRefreshLayout(swipeRefreshLayout: SwipeRefreshLayout?) {
    this.swipeRefreshLayout = swipeRefreshLayout
  }

  fun attachRecyclerView(recyclerView: RecyclerView?) {
    this.recyclerView = recyclerView
  }

  private fun hide() {
    hideAllComponents()
    visibility = GONE
  }

  private fun hideAllComponents() {
    if (swipeRefreshLayout != null && swipeRefreshLayout!!.isRefreshing) {
      swipeRefreshLayout!!.isRefreshing = false
    }
    emptyBox!!.visibility = GONE
    errorBox!!.visibility = GONE
    progress!!.visibility = GONE
    visibility = VISIBLE
  }

  private fun hideProgressBar() {
    if (swipeRefreshLayout != null && swipeRefreshLayout!!.isRefreshing) {
      swipeRefreshLayout!!.isRefreshing = false
    }
    progress!!.visibility = GONE
  }

  fun showProgress(shouldShow: Boolean) {
    if (shouldShow && swipeRefreshLayout != null && swipeRefreshLayout!!.isRefreshing) {
      return
    }
    if (shouldShow) {
      if (swipeRefreshLayout != null && recyclerView != null && recyclerView!!.adapter != null && recyclerView!!.adapter!!
          .itemCount > 0 && recyclerView!!.visibility == VISIBLE
      ) {
        hide()
      } else {
        hideAllComponents()
        progress!!.visibility = VISIBLE
      }
    } else {
      hideProgressBar()
    }
  }

  fun showError(
    message: String?,
    onTryAgainClickListener: OnClickListener?
  ) {
    if (recyclerView != null && recyclerView!!.adapter != null && recyclerView!!.adapter!!
        .itemCount > 0
    ) {
      hide()
      Snackbar.make(
        this,
        (if (TextUtils.isEmpty(message)) context.getString(R.string.unknown_error) else message)!!,
        Snackbar.LENGTH_LONG
      )
        .show()
    } else {
      hideAllComponents()
      errorBox!!.visibility = VISIBLE
      messageTxt!!.text = message
      this.onTryAgainClickListener = onTryAgainClickListener
      messageTxt!!.visibility =
        if (TextUtils.isEmpty(message)) GONE else VISIBLE
      tryAgain!!.visibility =
        if (this.onTryAgainClickListener == null) GONE else VISIBLE
    }
  }

  fun showEmpty(view: View) {
    hideAllComponents()
    val lp = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    lp.gravity = Gravity.CENTER_VERTICAL
    view.layoutParams = lp
    emptyBox!!.visibility = VISIBLE
    emptyBox!!.removeAllViews()
    emptyBox!!.addView(view)
  }

  fun showOnlyProgress() {
    emptyBox!!.visibility = GONE
    errorBox!!.visibility = GONE
    tryAgain!!.visibility = GONE
    progress!!.visibility = VISIBLE
  }

  override fun onClick(v: View) {
    if (onTryAgainClickListener != null) {
      hide()
      onTryAgainClickListener!!.onClick(v)
    }
  }
}

package com.asfoundation.wallet.ui.transactions

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.EpoxyRecyclerView

class HeaderRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                                   defStyleAttr: Int = 0) :
    EpoxyRecyclerView(context, attrs, defStyleAttr) {

  override fun createLayoutManager(): LayoutManager {
    return object : LinearLayoutManager(context) {
      override fun canScrollVertically(): Boolean {
        return false
      }
    }
  }

  override fun canScrollVertically(direction: Int): Boolean {
    return false
  }
}
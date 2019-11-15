package com.asfoundation.wallet.ui.widget.holder

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.ui.appcoins.CardNotificationsItemDecorator
import com.asfoundation.wallet.ui.widget.adapter.CardNotificationsAdapter
import rx.functions.Action2
import java.util.*

class CardNotificationsListViewHolder(resId: Int, parent: ViewGroup,
                                      action: Action2<CardNotification, CardNotificationAction>
) : BinderViewHolder<List<CardNotification>>(resId, parent) {

  private val adapter: CardNotificationsAdapter
  private val recyclerView: RecyclerView = findViewById(R.id.recycler_view)

  init {
    recyclerView.addItemDecoration(CardNotificationsItemDecorator())
    recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    adapter = CardNotificationsAdapter(ArrayList(), action)
    recyclerView.adapter = adapter
  }

  override fun bind(data: List<CardNotification>?, addition: Bundle) {
    if (data == null || data.isEmpty()) {
      recyclerView.visibility = View.GONE
    } else {
      adapter.notifications = data
      adapter.notifyDataSetChanged()
      recyclerView.visibility = View.VISIBLE
    }
  }

  companion object {
    const val VIEW_TYPE = 1008
  }
}

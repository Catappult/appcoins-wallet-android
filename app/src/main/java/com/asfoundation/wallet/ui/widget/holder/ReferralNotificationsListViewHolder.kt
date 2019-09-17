package com.asfoundation.wallet.ui.widget.holder

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.referrals.ReferralNotification
import com.asfoundation.wallet.ui.appcoins.ReferralNotificationsItemDecorator
import com.asfoundation.wallet.ui.widget.adapter.ReferralNotificationsAdapter
import rx.functions.Action2
import java.util.*

class ReferralNotificationsListViewHolder(resId: Int, parent: ViewGroup,
                                          action: Action2<ReferralNotification, ReferralNotificationAction>
) : BinderViewHolder<List<ReferralNotification>>(resId, parent) {

  private val adapter: ReferralNotificationsAdapter
  private val recyclerView: RecyclerView = findViewById(R.id.recycler_view)

  init {
    recyclerView.addItemDecoration(ReferralNotificationsItemDecorator())
    recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    adapter = ReferralNotificationsAdapter(ArrayList(), action)
    recyclerView.adapter = adapter
  }

  override fun bind(data: List<ReferralNotification>?, addition: Bundle) {
    if (data == null || data.isEmpty()) {
      recyclerView.visibility = View.GONE
    } else {
      adapter.notifications = data
      recyclerView.visibility = View.VISIBLE
    }
  }

  companion object {
    const val VIEW_TYPE = 1008
  }
}

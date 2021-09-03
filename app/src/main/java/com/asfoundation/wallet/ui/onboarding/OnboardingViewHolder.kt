package com.asfoundation.wallet.ui.onboarding

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_page_intro.view.*

class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  fun bind(item: OnboardingItem) {
    itemView.title.setText(item.title)
    itemView.message.text = item.message
  }

}
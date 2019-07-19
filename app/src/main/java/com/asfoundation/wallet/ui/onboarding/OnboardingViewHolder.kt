package com.asfoundation.wallet.ui.onboarding

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_page_intro.view.*

class OnboardingViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

  fun bind(title: Int, message: Int) {
    itemView.title.setText(title)
    itemView.message.setText(message)
  }

}
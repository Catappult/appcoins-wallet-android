package com.asfoundation.wallet.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R

class OnboardingPageAdapter(private var items: List<OnboardingItem>) :
    RecyclerView.Adapter<OnboardingViewHolder>() {

  override fun getItemCount() = items.size

  override fun onCreateViewHolder(container: ViewGroup, viewType: Int): OnboardingViewHolder {
    val view = LayoutInflater.from(container.context)
        .inflate(R.layout.layout_page_intro, container, false)
    return OnboardingViewHolder(view)
  }

  override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
    holder.bind(items[position])
  }
}

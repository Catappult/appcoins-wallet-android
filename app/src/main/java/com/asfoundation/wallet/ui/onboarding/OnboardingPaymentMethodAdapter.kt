package com.asfoundation.wallet.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R

class OnboardingPaymentMethodAdapter(private var items: List<String>) :
    RecyclerView.Adapter<OnboardingPaymentMethodViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup,
                                  viewType: Int): OnboardingPaymentMethodViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.layout_page_intro_payment_icon, parent, false)
    return OnboardingPaymentMethodViewHolder(view)
  }

  override fun onBindViewHolder(holder: OnboardingPaymentMethodViewHolder, position: Int) {
    holder.bind(items[position])
  }

  override fun getItemCount() = items.size

}
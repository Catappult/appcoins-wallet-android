package com.asfoundation.wallet.ui.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R

class OnboardingPageAdapter(maxAmount: String, context: Context) :
    RecyclerView.Adapter<OnboardingViewHolder>() {

  private val titles =
      intArrayOf(R.string.intro_1_title, R.string.intro_2_title, R.string.intro_3_title,
          R.string.referral_onboarding_title)
  private val messages =
      arrayOf(context.getString(R.string.intro_1_body), context.getString(R.string.intro_2_body),
          context.getString(R.string.intro_3_body),
          context.getString(R.string.referral_onboarding_body, maxAmount))

  override fun getItemCount(): Int {
    return titles.size
  }

  override fun onCreateViewHolder(container: ViewGroup, viewType: Int): OnboardingViewHolder {
    val view = LayoutInflater.from(container.context)
        .inflate(R.layout.layout_page_intro, container, false)
    return OnboardingViewHolder(view)
  }

  override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
    holder.bind(titles[position], messages[position])
  }

}

package com.asfoundation.wallet.ui.onboarding

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.asfoundation.wallet.GlideApp
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.layout_page_intro_payment_icon.view.*

class OnboardingPaymentMethodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  fun bind(imageSrc: String) {
    val imageView = itemView.payment_icon as ImageView

    GlideApp.with(itemView)
        .load(imageSrc)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(imageView)
  }
}
package com.asfoundation.wallet.subscriptions

import android.graphics.Bitmap
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asf.wallet.R
import com.asf.wallet.databinding.SubscriptionNewItemBinding
import com.asfoundation.wallet.GlideApp
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.Locale

class SubscriptionViewHolder(itemView: View, private val currencyFormatUtils: CurrencyFormatUtils) :
  RecyclerView.ViewHolder(itemView) {

  private val binding by lazy { SubscriptionNewItemBinding.bind(itemView) }

  fun bind(
    item: SubscriptionItem, clickCallback: PublishSubject<Pair<SubscriptionItem, View>>?,
    position: Int
  ) {
    itemView.apply {
      binding.appName.text = item.appName
      binding.appIcon.transitionName = "app_name_transition $position"

      when (item.status){
        Status.ACTIVE -> {
          if(item.isFreeTrial) {
            binding.statusBadge.visibility = View.VISIBLE
            binding.statusBadge.text = context.getString(R.string.subscriptions_free_trial_title)
            binding.statusBadge.background =
              ContextCompat.getDrawable(context, R.drawable.bg_badge_blue)
          } else {
            binding.statusBadge.visibility = View.GONE
          }
        }
        Status.CANCELED -> {
          binding.statusBadge.visibility = View.VISIBLE
          binding.statusBadge.text = context.getString(R.string.subscriptions_cancelled_badge)
          binding.statusBadge.background =
            ContextCompat.getDrawable(context, R.drawable.bg_badge_red)
        }
        Status.EXPIRED -> {
          binding.statusBadge.visibility = View.VISIBLE
          binding.statusBadge.text = context.getString(R.string.subscriptions_expired_badge)
          binding.statusBadge.background =
            ContextCompat.getDrawable(context, R.drawable.bg_badge_grey)
        }
        else -> { }
      }

      if ((item.status == Status.CANCELED || item.status == Status.PAUSED)) {
        showToExpireInfo(this, item)
      } else {
        showPriceInfo(this, item)
      }
      binding.itemParent.setOnClickListener { clickCallback?.onNext(Pair(item, binding.appIcon)) }
    }

    GlideApp.with(itemView.context)
      .asBitmap()
      .load(item.appIcon)
      .apply { RequestOptions().dontTransform() }
      .listener(SkeletonGlideRequestListener(binding.appIconSkeleton.root))
      .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
      .into(binding.appIcon)
  }

  private fun showPriceInfo(view: View, item: SubscriptionItem) {
    val formattedAmount = currencyFormatUtils.formatCurrency(item.fiatAmount)
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    item.expiry?.let {
      binding.expiresOn.visibility = View.VISIBLE
      binding.expiresOn.text = view.context.getString(
        R.string.subscriptions_renew_body,
        dateFormat.format(it)
      )
    }
    binding.recurrenceValue.visibility = View.VISIBLE

    binding.recurrenceValue.text =
      view.context.getString(R.string.value_fiat, formattedAmount, item.fiatSymbol)
    binding.recurrencePeriod.text = item.period?.mapToFrequency(view.context)

  }

  private fun showToExpireInfo(view: View, item: SubscriptionItem) {
    binding.recurrenceValue.visibility = View.GONE
    binding.expiresOn.visibility = View.VISIBLE

    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    item.expiry?.let {
      binding.expiresOn.text = view.context.getString(
        R.string.subscriptions_expiration_body,
        dateFormat.format(it)
      )
    }
  }
}

internal class SkeletonGlideRequestListener(private val skeleton: View) : RequestListener<Bitmap> {

  override fun onLoadFailed(
    e: GlideException?, model: Any?, target: Target<Bitmap>?,
    isFirstResource: Boolean
  ): Boolean {
    return true
  }

  override fun onResourceReady(
    resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
    dataSource: DataSource?, isFirstResource: Boolean
  ): Boolean {
    skeleton.visibility = View.GONE
    return false
  }
}
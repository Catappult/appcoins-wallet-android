package com.asfoundation.wallet.subscriptions

import android.graphics.Bitmap
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asf.wallet.databinding.SubscriptionItemBinding
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.*

class SubscriptionViewHolder(itemView: View, private val currencyFormatUtils: CurrencyFormatUtils) :
    RecyclerView.ViewHolder(itemView) {

    private val binding by lazy { SubscriptionItemBinding.bind(itemView) }

  fun bind(item: SubscriptionItem, clickCallback: PublishSubject<Pair<SubscriptionItem, View>>?,
           position: Int) {
    itemView.apply {
        binding.appName.text = item.appName
        binding.appIcon.transitionName = "app_name_transition $position"

      if ((item.status == Status.CANCELED || item.status == Status.PAUSED)) {
        showToExpireInfo(this, item)
      } else {
        showPriceInfo(this, item)
      }
        binding.moreButton.setOnClickListener { clickCallback?.onNext(Pair(item, binding.appIcon)) }
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
      binding.expiresOn.visibility = View.GONE
      binding.recurrenceValue.visibility = View.VISIBLE

    item.period?.let {
        binding.recurrenceValue.text = it.mapToSubsFrequency(view.context,
          view.context.getString(R.string.value_fiat, formattedAmount, item.fiatSymbol))
    }
  }

  private fun showToExpireInfo(view: View, item: SubscriptionItem) {
      binding.recurrenceValue.visibility = View.GONE
      binding.expiresOn.visibility = View.VISIBLE

    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    item.expiry?.let {
        binding.expiresOn.text = view.context.getString(R.string.subscriptions_expiration_body,
          dateFormat.format(it))
    }
  }
}

internal class SkeletonGlideRequestListener(private val skeleton: View) : RequestListener<Bitmap> {

  override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                            isFirstResource: Boolean): Boolean {
    return true
  }

  override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
                               dataSource: DataSource?, isFirstResource: Boolean): Boolean {
    skeleton.visibility = View.GONE
    return false
  }
}
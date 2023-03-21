package com.asfoundation.wallet.subscriptions

import android.graphics.Bitmap
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.subscription_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class SubscriptionViewHolder(itemView: View, private val currencyFormatUtils: CurrencyFormatUtils) :
    RecyclerView.ViewHolder(itemView) {

  fun bind(item: SubscriptionItem, clickCallback: PublishSubject<Pair<SubscriptionItem, View>>?,
           position: Int) {
    itemView.apply {
      app_name.text = item.appName
      app_icon.transitionName = "app_name_transition $position"

      if ((item.status == Status.CANCELED || item.status == Status.PAUSED)) {
        showToExpireInfo(this, item)
      } else {
        showPriceInfo(this, item)
      }
      more_button.setOnClickListener { clickCallback?.onNext(Pair(item, app_icon)) }
      item_parent.setOnClickListener { clickCallback?.onNext(Pair(item, app_icon)) }
    }

    GlideApp.with(itemView.context)
        .asBitmap()
        .load(item.appIcon)
        .apply { RequestOptions().dontTransform() }
        .listener(SkeletonGlideRequestListener(itemView.app_icon_skeleton))
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .into(itemView.app_icon)
  }

  private fun showPriceInfo(view: View, item: SubscriptionItem) {
    val formattedAmount = currencyFormatUtils.formatCurrency(item.fiatAmount)
    view.expires_on.visibility = View.GONE
    view.recurrence_value.visibility = View.VISIBLE

    item.period?.let {
      view.recurrence_value.text = it.mapToSubsFrequency(view.context,
          view.context.getString(R.string.value_fiat, formattedAmount, item.fiatSymbol))
    }
  }

  private fun showToExpireInfo(view: View, item: SubscriptionItem) {
    view.recurrence_value.visibility = View.GONE
    view.expires_on.visibility = View.VISIBLE

    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    item.expiry?.let {
      view.expires_on.text = view.context.getString(R.string.subscriptions_expiration_body,
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
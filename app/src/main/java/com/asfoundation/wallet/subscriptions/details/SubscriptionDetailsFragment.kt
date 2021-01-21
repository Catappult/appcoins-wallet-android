package com.asfoundation.wallet.subscriptions.details

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.subscriptions.Status
import com.asfoundation.wallet.subscriptions.SubscriptionItem
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_subscription_details.*
import kotlinx.android.synthetic.main.layout_active_subscription_content.*
import kotlinx.android.synthetic.main.layout_expired_subscription_content.*
import kotlinx.android.synthetic.main.layout_expired_subscription_content.view.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class SubscriptionDetailsFragment : DaggerFragment(), SubscriptionDetailsView {

  @Inject
  lateinit var currencyFormatUtils: CurrencyFormatUtils

  @Inject
  lateinit var presenter: SubscriptionDetailsPresenter

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_subscription_details, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    presenter.present()
  }

  override fun getCancelClicks() = RxView.clicks(cancel_subscription)

  override fun setActiveDetails(subscriptionItem: SubscriptionItem) {
    layout_expired_subscription_content.visibility = View.GONE
    cancel_subscription.visibility = View.VISIBLE
    layout_active_subscription_content.visibility = View.VISIBLE
    context?.let {
      status.setTextColor(ContextCompat.getColor(it, R.color.green))
      GlideApp.with(it)
          .asBitmap()
          .load(subscriptionItem.appIcon)
          .into(target)
      GlideApp.with(it)
          .load(subscriptionItem.paymentIcon)
          .into(layout_active_subscription_content.payment_method_icon)
    }

    app_name.text = subscriptionItem.appName
    status.text = getString(R.string.subscriptions_active_title)
    val formattedAmount = currencyFormatUtils.formatCurrency(subscriptionItem.fiatAmount)
    total_value.text = subscriptionItem.period?.mapToSubFrequency(requireContext(),
        getString(R.string.value_fiat, subscriptionItem.fiatSymbol, formattedAmount))
    total_value_appc.text = String.format("~%s / APPC",
        currencyFormatUtils.formatCurrency(subscriptionItem.appcAmount, WalletCurrency.CREDITS))

    layout_active_subscription_content.payment_method_value.text = subscriptionItem.paymentMethod

    if (shouldShowExpireOn(subscriptionItem)) {
      setExpireOnDetails(subscriptionItem)
    } else if (subscriptionItem.renewal != null) {
      next_payment_value.text = getDateString(subscriptionItem.renewal)
    }
  }

  private fun shouldShowExpireOn(subscriptionItem: SubscriptionItem): Boolean {
    return (subscriptionItem.status == Status.CANCELED || subscriptionItem.status == Status.PAUSED)
        && subscriptionItem.expire != null
  }

  private fun setExpireOnDetails(subscriptionItem: SubscriptionItem) {
    expires_on.visibility = View.VISIBLE
    cancel_subscription.visibility = View.GONE
    next_payment_value.text = getString(R.string.subscriptions_canceled_body)
    next_payment_value.setTextColor(resources.getColor(R.color.red))

    val dateFormat = SimpleDateFormat("MMM yy", Locale.getDefault())

    expires_on.text = subscriptionItem.expire?.let {
      getString(R.string.subscriptions_details_cancelled_body,
          dateFormat.format(it))
    }

    info.visibility = View.GONE
    info_text.visibility = View.GONE
  }

  override fun setExpiredDetails(subscriptionItem: SubscriptionItem) {
    layout_active_subscription_content.visibility = View.GONE
    layout_expired_subscription_content.visibility = View.VISIBLE
    info.visibility = View.GONE
    info_text.visibility = View.GONE
    cancel_subscription.visibility = View.GONE
    context?.let {
      status.setTextColor(ContextCompat.getColor(it, R.color.red))
      GlideApp.with(it)
          .asBitmap()
          .load(subscriptionItem.appIcon)
          .into(target)
      GlideApp.with(it)
          .load(subscriptionItem.paymentIcon)
          .into(layout_expired_subscription_content.payment_method_icon)
    }

    app_name.text = subscriptionItem.appName
    status.text = getString(R.string.subscriptions_expired_title)

    last_bill_value.text = subscriptionItem.ended?.let { getDateString(it) }
    start_date_value.text = subscriptionItem.started?.let { getDateString(it) }
    layout_expired_subscription_content.payment_method_value.text =
        subscriptionItem.paymentMethod
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    activity?.title = getString(R.string.subscriptions_title)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  private fun getDateString(date: Date): String {
    return DateFormat.format("dd MMM yyyy", date)
        .toString()
  }

  private val target = object : Target<Bitmap> {

    override fun onLoadStarted(placeholder: Drawable?) {
      app_icon.visibility = View.INVISIBLE
      app_icon_animation.visibility = View.VISIBLE
      app_icon_animation.playAnimation()
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
      app_icon.visibility = View.INVISIBLE
      app_icon_animation.visibility = View.VISIBLE
      app_icon_animation.repeatCount = 1
      app_icon_animation.playAnimation()
    }

    override fun getSize(cb: SizeReadyCallback) {
      cb.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
    }

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
      app_icon.visibility = View.VISIBLE
      app_icon_animation.visibility = View.INVISIBLE
      app_icon.setImageBitmap(resource)
    }

    override fun getRequest(): Request? = null
    override fun setRequest(request: Request?) = Unit
    override fun removeCallback(cb: SizeReadyCallback) = Unit
    override fun onLoadCleared(placeholder: Drawable?) = Unit
    override fun onStart() = Unit
    override fun onDestroy() = Unit
    override fun onStop() = Unit
  }

  companion object {

    const val SUBSCRIPTION_ITEM = "subscription_item"

    fun newInstance(subscriptionItem: SubscriptionItem): SubscriptionDetailsFragment {
      return SubscriptionDetailsFragment()
          .apply {
            arguments = Bundle().apply {
              putSerializable(SUBSCRIPTION_ITEM, subscriptionItem)
            }
          }
    }
  }
}
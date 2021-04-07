package com.asfoundation.wallet.subscriptions.details

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.subscriptions.Status
import com.asfoundation.wallet.subscriptions.SubscriptionItem
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestOptions
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

  override fun onAttach(context: Context) {
    super.onAttach(context)
    activity?.title = getString(R.string.subscriptions_title)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_subscription_details, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    presenter.present()
  }

  override fun getCancelClicks() = RxView.clicks(cancel_subscription)

  override fun retrieveSharedElement(): View {
    return app_icon
  }

  override fun setupTransitionName(transitionName: String) {
    app_icon.transitionName = transitionName
  }

  override fun setActiveDetails(subscriptionItem: SubscriptionItem) {
    app_name.text = subscriptionItem.appName

    layout_expired_subscription_content.visibility = View.GONE
    layout_active_subscription_content.visibility = View.VISIBLE

    status.text = getString(R.string.subscriptions_active_title)
    layout_active_subscription_content.payment_method_value.text = subscriptionItem.paymentMethod
    status.setTextColor(ResourcesCompat.getColor(resources, R.color.green, null))

    context?.let { loadImages(it, subscriptionItem.appIcon, subscriptionItem.paymentIcon) }
    setBillingInfo(subscriptionItem)

    if (subscriptionItem.status == Status.CANCELED) {
      setCanceledInfo(subscriptionItem)
    } else {
      cancel_subscription.visibility = View.VISIBLE
      subscriptionItem.renewal?.let { next_payment_value.text = formatDate(it) }
    }
  }

  private fun setBillingInfo(subscriptionItem: SubscriptionItem) {
    val formattedAmount = currencyFormatUtils.formatCurrency(subscriptionItem.fiatAmount)
    total_value.text = subscriptionItem.period?.mapToSubsFrequency(requireContext(),
        getString(R.string.value_fiat, subscriptionItem.fiatSymbol, formattedAmount))
    total_value_appc.text = String.format("~%s / APPC",
        currencyFormatUtils.formatCurrency(subscriptionItem.appcAmount, WalletCurrency.CREDITS))

  }

  override fun setExpiredDetails(subscriptionItem: SubscriptionItem) {
    app_name.text = subscriptionItem.appName

    layout_active_subscription_content.visibility = View.GONE
    layout_expired_subscription_content.visibility = View.VISIBLE
    info.visibility = View.GONE
    info_text.visibility = View.GONE
    cancel_subscription.visibility = View.GONE

    status.setTextColor(ResourcesCompat.getColor(resources, R.color.red, null))
    status.text = getString(R.string.subscriptions_expired_title)
    context?.let { loadImages(it, subscriptionItem.appIcon, subscriptionItem.paymentIcon) }

    subscriptionItem.ended?.let { last_bill_value.text = formatDate(it) }
    subscriptionItem.started?.let { start_date_value.text = formatDate(it) }
    layout_expired_subscription_content.payment_method_value.text =
        subscriptionItem.paymentMethod
  }

  private fun setCanceledInfo(subscriptionItem: SubscriptionItem) {
    expires_on.visibility = View.VISIBLE
    cancel_subscription.visibility = View.GONE
    next_payment_value.text = getString(R.string.subscriptions_canceled_body)
    next_payment_value.setTextColor(ResourcesCompat.getColor(resources, R.color.red, null))

    val dateFormat = SimpleDateFormat("MMM yy", Locale.getDefault())

    subscriptionItem.expiry?.let {
      expires_on.text = getString(R.string.subscriptions_details_cancelled_body,
          dateFormat.format(it))
    }

    info.visibility = View.GONE
    info_text.visibility = View.GONE
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  private fun formatDate(date: Date): String {
    return DateFormat.format("dd MMM yyyy", date)
        .toString()
  }

  private fun loadImages(context: Context, appIcon: String, paymentIcon: String) {
    GlideApp.with(context)
        .asBitmap()
        .load(appIcon)
        .apply { RequestOptions().dontTransform() }
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .into(target)
    GlideApp.with(context)
        .load(paymentIcon)
        .into(layout_active_subscription_content.payment_method_icon)
  }

  private val target = object : Target<Bitmap> {

    override fun onLoadStarted(placeholder: Drawable?) {
      app_icon.visibility = View.INVISIBLE
      app_icon_skeleton.visibility = View.VISIBLE
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
      startPostponedEnterTransition()
      app_icon.visibility = View.INVISIBLE
      app_icon_skeleton.visibility = View.VISIBLE
    }

    override fun getSize(cb: SizeReadyCallback) {
      cb.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
    }

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
      startPostponedEnterTransition()
      app_icon.visibility = View.VISIBLE
      app_icon_skeleton.visibility = View.INVISIBLE
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

    const val SUBSCRIPTION_ITEM_KEY = "subscription_item"
    const val TRANSITION_NAME_KEY = "transition_name"

    fun newInstance(subscriptionItem: SubscriptionItem,
                    transitionName: String): SubscriptionDetailsFragment {
      return SubscriptionDetailsFragment()
          .apply {
            arguments = Bundle().apply {
              putSerializable(SUBSCRIPTION_ITEM_KEY, subscriptionItem)
              putString(TRANSITION_NAME_KEY, transitionName)
            }
          }
    }
  }
}
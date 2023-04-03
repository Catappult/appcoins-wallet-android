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
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.databinding.FragmentSubscriptionDetailsBinding
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SubscriptionDetailsFragment : BasePageViewFragment(), SubscriptionDetailsView {

  @Inject
  lateinit var currencyFormatUtils: CurrencyFormatUtils

  @Inject
  lateinit var presenter: SubscriptionDetailsPresenter

  private var _binding: FragmentSubscriptionDetailsBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  // fragment_subscription_details.xml
  private val cancel_subscription get() = binding.cancelSubscription
  private val renew_subscription get() = binding.renewSubscription
  private val app_icon get() = binding.appIcon
  private val main_layout get() = binding.mainLayout
  private val app_icon_skeleton get() = binding.appIconSkeleton.root
  private val app_name get() = binding.appName
  private val sku_name get() = binding.skuName
  private val status_icon get() = binding.statusIcon
  private val status get() = binding.status
  private val expires_on get() = binding.expiresOn
  private val info get() = binding.info
  private val info_text get() = binding.infoText
  private val loading_animation get() = binding.loadingAnimation
  private val no_network_retry_only_layout get() = binding.noNetworkRetryOnlyLayout.root
  private val generic_error_retry_only_layout get() = binding.genericErrorRetryOnlyLayout.root
  private val layout_expired_subscription_content get() = binding.layoutExpiredSubscriptionContent.root
  private val layout_active_subscription_content get() = binding.layoutActiveSubscriptionContent.root

  // layout_expired_subscription_content.xml
  private val expired_payment_method_value get() = binding.layoutExpiredSubscriptionContent.paymentMethodValue
  private val last_bill_value get() = binding.layoutExpiredSubscriptionContent.lastBillValue
  private val start_date_value get() = binding.layoutExpiredSubscriptionContent.startDateValue

  // generic_error_retry_only_layout.xml
  private val generic_retry_button get() = binding.genericErrorRetryOnlyLayout.genericRetryButton

  // layout_active_subscription_content.xml
  private val total_value get() = binding.layoutActiveSubscriptionContent.totalValue
  private val total_value_appc get() = binding.layoutActiveSubscriptionContent.totalValueAppc
  private val next_payment_value get() = binding.layoutActiveSubscriptionContent.nextPaymentValue
  private val active_payment_method_value get() = binding.layoutActiveSubscriptionContent.paymentMethodValue
  private val active_payment_method_icon get() = binding.layoutActiveSubscriptionContent.paymentMethodIcon

  // no_network_retry_only_layout.xml
  private val retry_button get() = binding.noNetworkRetryOnlyLayout.retryButton
  private val retry_animation get() = binding.noNetworkRetryOnlyLayout.retryAnimation


  override fun onAttach(context: Context) {
    super.onAttach(context)
    activity?.title = getString(R.string.subscriptions_title)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    _binding = FragmentSubscriptionDetailsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    presenter.present()
  }

  override fun getCancelClicks() = RxView.clicks(cancel_subscription)

  override fun getRenewSubscriptionClicks() = RxView.clicks(renew_subscription)

  override fun getRetryClicks() =
      Observable.merge(RxView.clicks(generic_retry_button), RxView.clicks(retry_button))

  override fun retrieveSharedElement(): View {
    return app_icon
  }

  override fun showNoNetworkError() {
    retry_animation.visibility = View.GONE
    main_layout.visibility = View.GONE
    retry_button.visibility = View.VISIBLE
    loading_animation.visibility = View.GONE
    no_network_retry_only_layout.visibility = View.VISIBLE
    cancel_subscription.visibility = View.GONE
    renew_subscription.visibility = View.GONE
  }

  override fun showRenewError() {
    cancel_subscription.visibility = View.GONE
    renew_subscription.visibility = View.GONE
    no_network_retry_only_layout.visibility = View.GONE
    generic_error_retry_only_layout.visibility = View.VISIBLE
    loading_animation.visibility = View.GONE
    main_layout.visibility = View.GONE
  }

  override fun showDetails() {
    cancel_subscription.visibility = View.GONE
    renew_subscription.visibility = View.GONE
    no_network_retry_only_layout.visibility = View.GONE
    generic_error_retry_only_layout.visibility = View.GONE
    loading_animation.visibility = View.GONE
    main_layout.visibility = View.VISIBLE
  }

  override fun showLoading() {
    cancel_subscription.visibility = View.GONE
    renew_subscription.visibility = View.GONE
    main_layout.visibility = View.GONE
    loading_animation.visibility = View.VISIBLE
  }

  override fun setupTransitionName(transitionName: String) {
    app_icon.transitionName = transitionName
  }

  override fun setActiveDetails(subscriptionItem: SubscriptionItem) {
    app_name.text = subscriptionItem.appName

    layout_expired_subscription_content.visibility = View.GONE
    layout_active_subscription_content.visibility = View.VISIBLE

    status.text = getString(R.string.subscriptions_active_title)
    status_icon.setImageResource(R.drawable.ic_active)
    active_payment_method_value.text = subscriptionItem.paymentMethod
    status.setTextColor(ResourcesCompat.getColor(resources, R.color.styleguide_green, null))

    sku_name.text = subscriptionItem.itemName
    context?.let { loadImages(it, subscriptionItem.appIcon, subscriptionItem.paymentIcon) }
    setBillingInfo(subscriptionItem)

    if (subscriptionItem.status == Status.CANCELED) {
      setCanceledInfo(subscriptionItem)
      renew_subscription.visibility = View.VISIBLE
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
    sku_name.text = subscriptionItem.itemName

    layout_active_subscription_content.visibility = View.GONE
    layout_expired_subscription_content.visibility = View.VISIBLE
    info.visibility = View.GONE
    info_text.visibility = View.GONE
    cancel_subscription.visibility = View.GONE

    renew_subscription.visibility = View.GONE
    status_icon.setImageResource(R.drawable.ic_forbidden)
    status.setTextColor(ResourcesCompat.getColor(resources, R.color.styleguide_medium_grey, null))
    status.text = getString(R.string.subscriptions_inactive_title)
    context?.let { loadImages(it, subscriptionItem.appIcon, subscriptionItem.paymentIcon) }

    subscriptionItem.ended?.let { last_bill_value.text = formatDate(it) }
    subscriptionItem.started?.let { start_date_value.text = formatDate(it) }
    expired_payment_method_value.text = subscriptionItem.paymentMethod
  }

  private fun setCanceledInfo(subscriptionItem: SubscriptionItem) {
    expires_on.visibility = View.VISIBLE
    cancel_subscription.visibility = View.GONE
    next_payment_value.text = getString(R.string.subscriptions_canceled_body)
    next_payment_value.setTextColor(ResourcesCompat.getColor(resources, R.color.styleguide_red, null))

    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    subscriptionItem.expiry?.let {
      expires_on.text = getString(R.string.subscriptions_details_cancelled_body,
          dateFormat.format(it))
    }
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
        .into(active_payment_method_icon)
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
      app_icon?.visibility = View.VISIBLE
      app_icon_skeleton?.visibility = View.INVISIBLE
      app_icon?.setImageBitmap(resource)
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
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
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentSubscriptionDetailsBinding
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.subscriptions.Status
import com.asfoundation.wallet.subscriptions.SubscriptionItem
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.jakewharton.rxbinding2.view.RxView
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
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

  private val binding by viewBinding(FragmentSubscriptionDetailsBinding::bind)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    activity?.title = getString(R.string.subscriptions_title)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = FragmentSubscriptionDetailsBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    presenter.present()
  }

  override fun getCancelClicks() = RxView.clicks(binding.cancelSubscription)

  override fun getRenewSubscriptionClicks() = RxView.clicks(binding.renewSubscription)

  override fun getRetryClicks() =
      Observable.merge(RxView.clicks(binding.genericErrorRetryOnlyLayout.genericRetryButton), RxView.clicks(binding.noNetworkRetryOnlyLayout.retryButton))

  override fun retrieveSharedElement(): View {
    return binding.appIcon
  }

  override fun showNoNetworkError() {
    binding.noNetworkRetryOnlyLayout.retryAnimation.visibility = View.GONE
    binding.mainLayout.visibility = View.GONE
    binding.noNetworkRetryOnlyLayout.retryButton.visibility = View.VISIBLE
    binding.loadingAnimation.visibility = View.GONE
    binding.noNetworkRetryOnlyLayout.root.visibility = View.VISIBLE
    binding.cancelSubscription.visibility = View.GONE
    binding.renewSubscription.visibility = View.GONE
  }

  override fun showRenewError() {
    binding.cancelSubscription.visibility = View.GONE
    binding.renewSubscription.visibility = View.GONE
    binding.noNetworkRetryOnlyLayout.root.visibility = View.GONE
    binding.genericErrorRetryOnlyLayout.root.visibility = View.VISIBLE
    binding.loadingAnimation.visibility = View.GONE
    binding.mainLayout.visibility = View.GONE
  }

  override fun showDetails() {
    binding.cancelSubscription.visibility = View.GONE
    binding.renewSubscription.visibility = View.GONE
    binding.noNetworkRetryOnlyLayout.root.visibility = View.GONE
    binding.genericErrorRetryOnlyLayout.root.visibility = View.GONE
    binding.loadingAnimation.visibility = View.GONE
    binding.mainLayout.visibility = View.VISIBLE
  }

  override fun showLoading() {
    binding.cancelSubscription.visibility = View.GONE
    binding.renewSubscription.visibility = View.GONE
    binding.mainLayout.visibility = View.GONE
    binding.loadingAnimation.visibility = View.VISIBLE
  }

  override fun setupTransitionName(transitionName: String) {
    binding.appIcon.transitionName = transitionName
  }

  override fun setActiveDetails(subscriptionItem: SubscriptionItem) {
    binding.appName.text = subscriptionItem.appName

    binding.layoutExpiredSubscriptionContent.root.visibility = View.GONE
    binding.layoutActiveSubscriptionContent.root.visibility = View.VISIBLE

    binding.status.text = getString(R.string.subscriptions_active_title)
    binding.statusIcon.setImageResource(R.drawable.ic_active)
    binding.layoutActiveSubscriptionContent.paymentMethodValue.text = subscriptionItem.paymentMethod
    binding.status.setTextColor(ResourcesCompat.getColor(resources, R.color.styleguide_green, null))

    binding.skuName.text = subscriptionItem.itemName
    context?.let { loadImages(it, subscriptionItem.appIcon, subscriptionItem.paymentIcon) }
    setBillingInfo(subscriptionItem)

    if (subscriptionItem.status == Status.CANCELED) {
      setCanceledInfo(subscriptionItem)
      binding.renewSubscription.visibility = View.VISIBLE
    } else {
      binding.cancelSubscription.visibility = View.VISIBLE
      subscriptionItem.renewal?.let { binding.layoutActiveSubscriptionContent.nextPaymentValue.text = formatDate(it) }
    }
  }

  private fun setBillingInfo(subscriptionItem: SubscriptionItem) {
    val formattedAmount = currencyFormatUtils.formatCurrency(subscriptionItem.fiatAmount)
    binding.layoutActiveSubscriptionContent.totalValue.text = subscriptionItem.period?.mapToSubsFrequency(requireContext(),
        getString(R.string.value_fiat, subscriptionItem.fiatSymbol, formattedAmount))
    binding.layoutActiveSubscriptionContent.totalValueAppc.text = String.format("~%s / APPC",
        currencyFormatUtils.formatCurrency(subscriptionItem.appcAmount, WalletCurrency.CREDITS))

  }

  override fun setExpiredDetails(subscriptionItem: SubscriptionItem) {
    binding.appName.text = subscriptionItem.appName
    binding.skuName.text = subscriptionItem.itemName

    binding.layoutActiveSubscriptionContent.root.visibility = View.GONE
    binding.layoutExpiredSubscriptionContent.root.visibility = View.VISIBLE
    binding.info.visibility = View.GONE
    binding.infoText.visibility = View.GONE
    binding.cancelSubscription.visibility = View.GONE

    binding.renewSubscription.visibility = View.GONE
    binding.statusIcon.setImageResource(R.drawable.ic_forbidden)
    binding.status.setTextColor(ResourcesCompat.getColor(resources, R.color.styleguide_medium_grey, null))
    binding.status.text = getString(R.string.subscriptions_inactive_title)
    context?.let { loadImages(it, subscriptionItem.appIcon, subscriptionItem.paymentIcon) }

    subscriptionItem.ended?.let { binding.layoutExpiredSubscriptionContent.lastBillValue.text = formatDate(it) }
    subscriptionItem.started?.let { binding.layoutExpiredSubscriptionContent.startDateValue.text = formatDate(it) }
    binding.layoutExpiredSubscriptionContent.paymentMethodValue.text = subscriptionItem.paymentMethod
  }

  private fun setCanceledInfo(subscriptionItem: SubscriptionItem) {
    binding.expiresOn.visibility = View.VISIBLE
    binding.cancelSubscription.visibility = View.GONE
    binding.layoutActiveSubscriptionContent.nextPaymentValue.text = getString(R.string.subscriptions_canceled_body)
    binding.layoutActiveSubscriptionContent.nextPaymentValue.setTextColor(ResourcesCompat.getColor(resources, R.color.styleguide_red, null))

    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    subscriptionItem.expiry?.let {
      binding.expiresOn.text = getString(R.string.subscriptions_details_cancelled_body,
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
        .into(binding.layoutActiveSubscriptionContent.paymentMethodIcon)
  }

  private val target = object : Target<Bitmap> {

    override fun onLoadStarted(placeholder: Drawable?) {
      binding.appIcon.visibility = View.INVISIBLE
      binding.appIconSkeleton.root.visibility = View.VISIBLE
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
      startPostponedEnterTransition()
      binding.appIcon.visibility = View.INVISIBLE
      binding.appIconSkeleton.root.visibility = View.VISIBLE
    }

    override fun getSize(cb: SizeReadyCallback) {
      cb.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
    }

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
      startPostponedEnterTransition()
      binding.appIcon.visibility = View.VISIBLE
      binding.appIconSkeleton.root.visibility = View.INVISIBLE
      binding.appIcon.setImageBitmap(resource)
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
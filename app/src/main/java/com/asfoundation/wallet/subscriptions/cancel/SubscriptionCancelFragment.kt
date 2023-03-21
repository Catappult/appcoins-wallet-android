package com.asfoundation.wallet.subscriptions.cancel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.subscriptions.SubscriptionItem
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_subscription_cancel.*
import kotlinx.android.synthetic.main.layout_subscription_info.*
import kotlinx.android.synthetic.main.no_network_retry_only_layout.*
import javax.inject.Inject

@AndroidEntryPoint
class SubscriptionCancelFragment : BasePageViewFragment(), SubscriptionCancelView {

  @Inject
  lateinit var currencyFormatUtils: CurrencyFormatUtils

  @Inject
  lateinit var presenter: SubscriptionCancelPresenter


  override fun onAttach(context: Context) {
    super.onAttach(context)
    activity?.title = getString(R.string.subscriptions_title)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_subscription_cancel, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun getBackClicks() = RxView.clicks(back_button)

  override fun getCancelClicks() = RxView.clicks(cancel_subscription)

  override fun showLoading() {
    error.visibility = View.GONE
    no_network_retry_only_layout.visibility = View.GONE
    layout_content.visibility = View.GONE
    loading_animation.visibility = View.VISIBLE
  }

  override fun showCancelError() {
    no_network_retry_only_layout.visibility = View.GONE
    loading_animation.visibility = View.GONE
    layout_content.visibility = View.VISIBLE
    error.visibility = View.VISIBLE
  }

  override fun showSubscriptionDetails(subscriptionItem: SubscriptionItem) {
    no_network_retry_only_layout.visibility = View.GONE
    error.visibility = View.GONE
    loading_animation.visibility = View.INVISIBLE
    layout_content.visibility = View.VISIBLE

    loadImage(subscriptionItem.appIcon)

    app_name.text = subscriptionItem.appName
    sku_name.text = subscriptionItem.itemName

    val formattedAmount = currencyFormatUtils.formatCurrency(subscriptionItem.fiatAmount)
    total_value.text = subscriptionItem.period?.mapToSubsFrequency(requireContext(),
        getString(R.string.value_fiat, subscriptionItem.fiatSymbol, formattedAmount))
    total_value_appc.text = String.format("~%s / APPC",
        currencyFormatUtils.formatCurrency(subscriptionItem.appcAmount, WalletCurrency.CREDITS))
  }

  private fun loadImage(appIcon: String) {
    val target = object : Target<Bitmap> {

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

    context?.let {
      GlideApp.with(it)
          .asBitmap()
          .load(appIcon)
          .apply { RequestOptions().dontTransform() }
          .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
          .into(target)
    }
  }

  override fun showNoNetworkError() {
    retry_animation.visibility = View.GONE
    layout_content.visibility = View.GONE
    error.visibility = View.GONE
    retry_button.visibility = View.VISIBLE
    loading_animation.visibility = View.GONE
    no_network_retry_only_layout.visibility = View.VISIBLE
  }

  override fun getRetryNetworkClicks() = RxView.clicks(retry_button)

  override fun showNoNetworkRetryAnimation() {
    retry_button.visibility = View.INVISIBLE
    retry_animation.visibility = View.VISIBLE
  }

  override fun setTransitionName(transitionName: String) {
    app_icon.transitionName = transitionName
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  companion object {

    const val SUBSCRIPTION_ITEM_KEY = "subscription_item"
    const val TRANSITION_NAME_KEY = "transition_name"

    fun newInstance(subscriptionItem: SubscriptionItem,
                    transitionName: String): SubscriptionCancelFragment {
      return SubscriptionCancelFragment()
          .apply {
            arguments = Bundle().apply {
              putSerializable(SUBSCRIPTION_ITEM_KEY, subscriptionItem)
              putString(TRANSITION_NAME_KEY, transitionName)
            }
          }
    }
  }
}
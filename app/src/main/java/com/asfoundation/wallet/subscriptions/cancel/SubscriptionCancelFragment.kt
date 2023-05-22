package com.asfoundation.wallet.subscriptions.cancel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.subscriptions.SubscriptionItem
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.databinding.FragmentSubscriptionCancelBinding
import com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SubscriptionCancelFragment : com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment(null), SubscriptionCancelView {

  @Inject
  lateinit var currencyFormatUtils: CurrencyFormatUtils

  @Inject
  lateinit var presenter: SubscriptionCancelPresenter

  private val binding by viewBinding(FragmentSubscriptionCancelBinding::bind)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    activity?.title = getString(R.string.subscriptions_title)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = FragmentSubscriptionCancelBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun getBackClicks() = RxView.clicks(binding.backButton)

  override fun getCancelClicks() = RxView.clicks(binding.cancelSubscription)

  override fun showLoading() {
    binding.error.visibility = View.GONE
    binding.noNetworkRetryOnlyLayout.root.visibility = View.GONE
    binding.layoutContent.visibility = View.GONE
    binding.loadingAnimation.visibility = View.VISIBLE
  }

  override fun showCancelError() {
    binding.noNetworkRetryOnlyLayout.root.visibility = View.GONE
    binding.loadingAnimation.visibility = View.GONE
    binding.layoutContent.visibility = View.VISIBLE
    binding.error.visibility = View.VISIBLE
  }

  override fun showSubscriptionDetails(subscriptionItem: SubscriptionItem) {
    binding.noNetworkRetryOnlyLayout.root.visibility = View.GONE
    binding.error.visibility = View.GONE
    binding.loadingAnimation.visibility = View.INVISIBLE
    binding.layoutContent.visibility = View.VISIBLE

    loadImage(subscriptionItem.appIcon)

    binding.layoutSubscriptionInfo.appName.text = subscriptionItem.appName
    binding.layoutSubscriptionInfo.skuName.text = subscriptionItem.itemName

    val formattedAmount = currencyFormatUtils.formatCurrency(subscriptionItem.fiatAmount)
    binding.layoutSubscriptionInfo.totalValue.text = subscriptionItem.period?.mapToSubsFrequency(requireContext(),
        getString(R.string.value_fiat, subscriptionItem.fiatSymbol, formattedAmount))
    binding.layoutSubscriptionInfo.totalValueAppc.text = String.format("~%s / APPC",
        currencyFormatUtils.formatCurrency(subscriptionItem.appcAmount, WalletCurrency.CREDITS))
  }

  private fun loadImage(appIcon: String) {
    val target = object : Target<Bitmap> {

      override fun onLoadStarted(placeholder: Drawable?) {
        binding.layoutSubscriptionInfo.appIcon.visibility = View.INVISIBLE
        binding.layoutSubscriptionInfo.appIconSkeleton.root.visibility = View.VISIBLE
      }

      override fun onLoadFailed(errorDrawable: Drawable?) {
        startPostponedEnterTransition()
        binding.layoutSubscriptionInfo.appIcon.visibility = View.INVISIBLE
        binding.layoutSubscriptionInfo.appIconSkeleton.root.visibility = View.VISIBLE
      }

      override fun getSize(cb: SizeReadyCallback) {
        cb.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
      }

      override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        startPostponedEnterTransition()
        binding.layoutSubscriptionInfo.appIcon.visibility = View.VISIBLE
        binding.layoutSubscriptionInfo.appIconSkeleton.root.visibility = View.INVISIBLE
        binding.layoutSubscriptionInfo.appIcon.setImageBitmap(resource)
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
    binding.noNetworkRetryOnlyLayout.retryAnimation.visibility = View.GONE
    binding.layoutContent.visibility = View.GONE
    binding.error.visibility = View.GONE
    binding.noNetworkRetryOnlyLayout.retryButton.visibility = View.VISIBLE
    binding.loadingAnimation.visibility = View.GONE
    binding.noNetworkRetryOnlyLayout.root.visibility = View.VISIBLE
  }

  override fun getRetryNetworkClicks() = RxView.clicks(binding.noNetworkRetryOnlyLayout.retryButton)

  override fun showNoNetworkRetryAnimation() {
    binding.noNetworkRetryOnlyLayout.retryButton.visibility = View.INVISIBLE
    binding.noNetworkRetryOnlyLayout.retryAnimation.visibility = View.VISIBLE
  }

  override fun setTransitionName(transitionName: String) {
    binding.layoutSubscriptionInfo.appIcon.transitionName = transitionName
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
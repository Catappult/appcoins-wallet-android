package com.asfoundation.wallet.subscriptions

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
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_subscription_details.*
import kotlinx.android.synthetic.main.generic_error_retry_only_layout.*
import kotlinx.android.synthetic.main.layout_active_subscription_content.*
import kotlinx.android.synthetic.main.layout_expired_subscription_content.*
import kotlinx.android.synthetic.main.layout_expired_subscription_content.view.*
import kotlinx.android.synthetic.main.no_network_retry_only_layout.*
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class SubscriptionDetailsFragment : DaggerFragment(), SubscriptionDetailsView {

  @Inject
  lateinit var subscriptionInteract: SubscriptionInteract

  private lateinit var presenter: SubscriptionDetailsPresenter
  private lateinit var activity: SubscriptionView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        SubscriptionDetailsPresenter(subscriptionInteract, CompositeDisposable(), Schedulers.io(),
            AndroidSchedulers.mainThread(), this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_subscription_details, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    presenter.present(packageName)
  }

  override fun getCancelClicks() = RxView.clicks(cancel_subscription)

  override fun cancelSubscription() = activity.showCancelSubscription(packageName)

  override fun setActiveDetails(subscriptionDetails: ActiveSubscriptionDetails) {
    layout_expired_subscription_content.visibility = View.GONE
    cancel_subscription.visibility = View.VISIBLE
    layout_active_subscription_content.visibility = View.VISIBLE
    context?.let {
      status.setTextColor(ContextCompat.getColor(it, R.color.green))
      GlideApp.with(it)
          .asBitmap()
          .load(subscriptionDetails.iconUrl)
          .into(target)
      GlideApp.with(it)
          .asBitmap()
          .load(subscriptionDetails.iconUrl)
          .into(target)
      GlideApp.with(it)
          .load(subscriptionDetails.paymentMethodUrl)
          .into(layout_active_subscription_content.payment_method_icon)
    }

    app_name.text = subscriptionDetails.appName
    status.text = getString(R.string.subscriptions_active_title)
    total_value.text = String.format("%s / %s",
        subscriptionDetails.symbol + subscriptionDetails.amount.setScale(FIAT_SCALE,
            RoundingMode.FLOOR), subscriptionDetails.recurrence)

    total_value_appc.text = String.format("~%s / APPC",
        subscriptionDetails.appcValue.setScale(FIAT_SCALE, RoundingMode.FLOOR))

    layout_active_subscription_content.payment_method_value.text = subscriptionDetails.paymentMethod

    if (subscriptionDetails.expiresOn != null) {
      setExpireOnDetails(subscriptionDetails)
    } else if (subscriptionDetails.nextPayment != null) {
      next_payment_value.text = getDateString(subscriptionDetails.nextPayment)
    }
  }

  private fun setExpireOnDetails(subscriptionDetails: ActiveSubscriptionDetails) {
    expires_on.visibility = View.VISIBLE
    cancel_subscription.visibility = View.GONE
    next_payment_value.text = getString(R.string.subscriptions_canceled_body)
    next_payment_value.setTextColor(resources.getColor(R.color.red))

    val dateFormat = SimpleDateFormat("MMM yy", Locale.getDefault())

    expires_on.text = getString(R.string.subscriptions_details_cancelled_body,
        dateFormat.format(subscriptionDetails.expiresOn))

    info.visibility = View.GONE
    info_text.visibility = View.GONE
  }

  override fun setExpiredDetails(subscriptionDetails: ExpiredSubscriptionDetails) {
    layout_active_subscription_content.visibility = View.GONE
    layout_expired_subscription_content.visibility = View.VISIBLE
    info.visibility = View.GONE
    info_text.visibility = View.GONE
    cancel_subscription.visibility = View.GONE
    context?.let {
      status.setTextColor(ContextCompat.getColor(it, R.color.red))
      GlideApp.with(it)
          .asBitmap()
          .load(subscriptionDetails.iconUrl)
          .into(target)
      GlideApp.with(it)
          .load(subscriptionDetails.paymentMethodUrl)
          .into(layout_expired_subscription_content.payment_method_icon)
    }

    app_name.text = subscriptionDetails.appName
    status.text = getString(R.string.subscriptions_expired_title)

    last_bill_value.text = getDateString(subscriptionDetails.lastBill)
    start_date_value.text = getDateString(subscriptionDetails.startDate)
    layout_expired_subscription_content.payment_method_value.text =
        subscriptionDetails.paymentMethod
  }

  override fun showNoNetworkError() {
    main_layout.visibility = View.GONE
    retry_animation.visibility = View.GONE
    retry_button.visibility = View.VISIBLE
    generic_retry_animation.visibility = View.GONE
    generic_error_retry_only_layout.visibility = View.GONE
    loading_animation.visibility = View.GONE
    no_network_retry_only_layout.visibility = View.VISIBLE
  }

  override fun showGenericError() {
    main_layout.visibility = View.GONE
    retry_animation.visibility = View.GONE
    generic_retry_animation.visibility = View.GONE
    generic_retry_button.visibility = View.VISIBLE
    loading_animation.visibility = View.GONE
    no_network_retry_only_layout.visibility = View.GONE
    generic_error_retry_only_layout.visibility = View.VISIBLE
  }

  override fun showDetails() {
    loading_animation.visibility = View.GONE
    generic_error_retry_only_layout.visibility = View.GONE
    no_network_retry_only_layout.visibility = View.GONE
    main_layout.visibility = View.VISIBLE
  }

  override fun showLoading() {
    main_layout.visibility = View.GONE
    generic_error_retry_only_layout.visibility = View.GONE
    no_network_retry_only_layout.visibility = View.GONE
    loading_animation.visibility = View.VISIBLE
  }

  override fun getRetryGenericClicks() = RxView.clicks(generic_retry_button)

  override fun getRetryNetworkClicks() = RxView.clicks(retry_button)

  override fun showNoNetworkRetryAnimation() {
    retry_button.visibility = View.INVISIBLE
    retry_animation.visibility = View.VISIBLE
  }

  override fun showGenericRetryAnimation() {
    generic_retry_button.visibility = View.INVISIBLE
    generic_retry_animation.visibility = View.VISIBLE
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    require(
        context is SubscriptionView) { SubscriptionDetailsFragment::class.java.simpleName + " needs to be attached to a " + SubscriptionActivity::class.java.simpleName }
    activity = context
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  private val packageName: String by lazy {
    if (arguments!!.containsKey(PACKAGE_NAME)) {
      arguments!!.getSerializable(PACKAGE_NAME) as String
    } else {
      throw IllegalArgumentException("package name not found")
    }
  }

  private fun getDateString(date: Date): String {
    return DateFormat.format("dd MMM yyyy", date)
        .toString()
  }

  private val target = object : Target<Bitmap> {

    override fun onLoadStarted(placeholder: Drawable?) {
      app_icon.visibility = View.GONE
      app_icon_animation.visibility = View.VISIBLE
      app_icon_animation.playAnimation()
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
      app_icon.visibility = View.GONE
      app_icon_animation.visibility = View.VISIBLE
      app_icon_animation.repeatCount = 1
      app_icon_animation.playAnimation()
    }

    override fun getSize(cb: SizeReadyCallback) {
      cb.onSizeReady(Target.SIZE_ORIGINAL,
          Target.SIZE_ORIGINAL)
    }

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
      app_icon.visibility = View.VISIBLE
      app_icon_animation.visibility = View.GONE
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

    private const val FIAT_SCALE = 2
    private const val PACKAGE_NAME = "package_name"

    fun newInstance(appName: String): SubscriptionDetailsFragment {
      return SubscriptionDetailsFragment()
          .apply { arguments = Bundle().apply { putString(PACKAGE_NAME, appName) } }
    }

  }

}
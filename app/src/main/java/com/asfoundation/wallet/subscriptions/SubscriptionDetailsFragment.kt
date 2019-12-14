package com.asfoundation.wallet.subscriptions

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import com.squareup.picasso.Picasso
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_subscription_details.*
import kotlinx.android.synthetic.main.layout_active_subscription_content.*
import kotlinx.android.synthetic.main.layout_expired_subscription_content.*
import kotlinx.android.synthetic.main.layout_expired_subscription_content.view.*
import java.math.RoundingMode
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

  override fun getBackClicks(): Observable<Any> {
    return RxView.clicks(back_button)
  }

  override fun getCancelClicks(): Observable<String> {
    return RxView.clicks(cancel_subscription)
        .map { packageName }
  }

  override fun cancelSubscription() {
    activity.showCancelSubscription(packageName)
  }

  override fun navigateBack() {
    activity.navigateBack()
  }

  override fun showActiveDetails(subscriptionDetails: SubscriptionDetails) {
    layout_expired_subscription_content.visibility = View.GONE
    cancel_subscription.visibility = View.VISIBLE
    layout_active_subscription_content.visibility = View.VISIBLE
    status.setTextColor(ContextCompat.getColor(context!!, R.color.green))

    Picasso.with(context)
        .load(subscriptionDetails.iconUrl)
        .into(app_icon)

    Picasso.with(context)
        .load(subscriptionDetails.paymentMethodUrl)
        .into(layout_active_subscription_content.payment_method_icon)

    app_name.text = subscriptionDetails.appName
    status.text = subscriptionDetails.status.name
    total_monthly.text = String.format("%s / month",
        subscriptionDetails.symbol + subscriptionDetails.amount.setScale(FIAT_SCALE,
            RoundingMode.FLOOR))

    total_monthly_appc.text = String.format("~%s / APPC",
        subscriptionDetails.appcValue.setScale(FIAT_SCALE, RoundingMode.FLOOR))

    next_payment_value.text = getDateString(subscriptionDetails.nextPayment!!)
    layout_active_subscription_content.payment_method_value.text = subscriptionDetails.paymentMethod
  }

  override fun showExpiredDetails(subscriptionDetails: SubscriptionDetails) {
    layout_active_subscription_content.visibility = View.GONE
    layout_expired_subscription_content.visibility = View.VISIBLE
    info.visibility = View.GONE
    info_text.visibility = View.GONE
    cancel_subscription.visibility = View.INVISIBLE
    status.setTextColor(ContextCompat.getColor(context!!, R.color.red))

    Picasso.with(context)
        .load(subscriptionDetails.iconUrl)
        .into(app_icon)

    Picasso.with(context)
        .load(subscriptionDetails.paymentMethodUrl)
        .into(layout_expired_subscription_content.payment_method_icon)

    app_name.text = subscriptionDetails.appName
    status.text = subscriptionDetails.status.name

    last_bill_value.text = getDateString(subscriptionDetails.lastBill!!)
    start_date_value.text = getDateString(subscriptionDetails.startDate!!)
    layout_expired_subscription_content.payment_method_value.text =
        subscriptionDetails.paymentMethod
  }

  override fun showNoNetworkError() {
    main_layout.visibility = View.GONE
    loading_animation.visibility = View.GONE
    no_network_retry_only_layout.visibility = View.VISIBLE
  }

  override fun showDetails() {
    loading_animation.visibility = View.GONE
    no_network_retry_only_layout.visibility = View.GONE
    main_layout.visibility = View.VISIBLE
  }

  override fun showLoading() {
    main_layout.visibility = View.GONE
    no_network_retry_only_layout.visibility = View.GONE
    loading_animation.visibility = View.VISIBLE
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

  companion object {

    private const val FIAT_SCALE = 2
    private const val PACKAGE_NAME = "package_name"

    fun newInstance(appName: String): SubscriptionDetailsFragment {
      return SubscriptionDetailsFragment()
          .apply { arguments = Bundle().apply { putString(PACKAGE_NAME, appName) } }
    }

  }

}
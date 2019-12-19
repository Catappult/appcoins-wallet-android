package com.asfoundation.wallet.subscriptions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import com.squareup.picasso.Picasso
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_subscription_cancel.*
import kotlinx.android.synthetic.main.layout_subscription_info.*
import java.math.RoundingMode
import javax.inject.Inject

class SubscriptionCancelFragment : DaggerFragment(), SubscriptionCancelView {

  @Inject
  lateinit var subscriptionInteract: SubscriptionInteract

  private lateinit var presenter: SubscriptionCancelPresenter
  private lateinit var activity: SubscriptionView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        SubscriptionCancelPresenter(this, subscriptionInteract, CompositeDisposable(),
            Schedulers.io(), AndroidSchedulers.mainThread())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_subscription_cancel, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    presenter.present(packageName)
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
    error.visibility = View.VISIBLE
  }

  override fun showCancelSuccess() {
    activity.showCancelSuccess()
  }

  override fun navigateBack() {
    activity.navigateBack()
  }

  override fun showSubscriptionDetails(subscriptionDetails: ActiveSubscriptionDetails) {
    no_network_retry_only_layout.visibility = View.GONE
    error.visibility = View.GONE
    loading_animation.visibility = View.GONE
    layout_content.visibility = View.VISIBLE

    Picasso.with(context)
        .load(subscriptionDetails.iconUrl)
        .into(app_icon)

    app_name.text = subscriptionDetails.appName

    total_value.text = String.format("%s / %s",
        subscriptionDetails.symbol + subscriptionDetails.amount.setScale(FIAT_SCALE,
            RoundingMode.FLOOR), subscriptionDetails.recurrence)
    total_value_appc.text = String.format("~%s / APPC",
        subscriptionDetails.appcValue.setScale(FIAT_SCALE, RoundingMode.FLOOR))
  }

  override fun showNoNetworkError() {
    loading_animation.visibility = View.GONE
    layout_content.visibility = View.GONE
    error.visibility = View.GONE
    no_network_retry_only_layout.visibility = View.VISIBLE
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    require(
        context is SubscriptionView) { SubscriptionCancelFragment::class.java.simpleName + " needs to be attached to a " + SubscriptionActivity::class.java.simpleName }
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

  companion object {

    private const val FIAT_SCALE = 2
    private const val PACKAGE_NAME = "package_name"

    fun newInstance(packageName: String): SubscriptionCancelFragment {
      return SubscriptionCancelFragment()
          .apply {
            arguments = Bundle().apply {
              putString(PACKAGE_NAME, packageName)
            }
          }
    }

  }

}
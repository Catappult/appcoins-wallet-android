package com.asfoundation.wallet.subscriptions.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import com.asf.wallet.R
import com.asfoundation.wallet.subscriptions.SubscriptionAdapter
import com.asfoundation.wallet.subscriptions.SubscriptionItem
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_subscription_list.*
import kotlinx.android.synthetic.main.generic_error_retry_only_layout.*
import kotlinx.android.synthetic.main.no_network_retry_only_layout.*
import javax.inject.Inject

class SubscriptionListFragment : DaggerFragment(), SubscriptionListView {

  @Inject
  lateinit var presenter: SubscriptionListPresenter
  private lateinit var activeAdapter: SubscriptionAdapter
  private lateinit var expiredAdapter: SubscriptionAdapter
  private var clickSubject: PublishSubject<SubscriptionItem>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    clickSubject = PublishSubject.create()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_subscription_list, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    activity?.title = getString(R.string.subscriptions_settings_title)
    activeAdapter = SubscriptionAdapter(clickSubject)
    expiredAdapter = SubscriptionAdapter(clickSubject)

    rvActiveSubs.adapter = activeAdapter
    rvActiveSubs.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    rvExpiredSubs.adapter = expiredAdapter
    rvExpiredSubs.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

    presenter.present()
  }

  override fun subscriptionClicks(): Observable<SubscriptionItem> = clickSubject!!

  override fun onActiveSubscriptions(subscriptionModels: List<SubscriptionItem>) {
    activeAdapter.submitList(subscriptionModels)
  }

  override fun onExpiredSubscriptions(subscriptionModels: List<SubscriptionItem>) {
    expiredAdapter.submitList(subscriptionModels)
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

  override fun showSubscriptions() {
    loading_animation.visibility = View.GONE
    no_network_retry_only_layout.visibility = View.GONE
    generic_error_retry_only_layout.visibility = View.GONE
    layout_no_subscriptions.visibility = View.GONE
    main_layout.visibility = View.VISIBLE
  }

  override fun showNoSubscriptions() {
    loading_animation.visibility = View.GONE
    main_layout.visibility = View.GONE
    generic_error_retry_only_layout.visibility = View.GONE
    no_network_retry_only_layout.visibility = View.GONE
    layout_no_subscriptions.visibility = View.VISIBLE
  }

  override fun showLoading() {
    main_layout.visibility = View.GONE
    no_network_retry_only_layout.visibility = View.GONE
    generic_error_retry_only_layout.visibility = View.GONE
    layout_no_subscriptions.visibility = View.GONE
    loading_animation.visibility = View.VISIBLE
  }

  override fun retryClick() = RxView.clicks(retry_button)

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

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  companion object {

    fun newInstance(): SubscriptionListFragment {
      return SubscriptionListFragment()
    }

  }

}
package com.asfoundation.wallet.subscriptions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_subscription_list.*
import kotlinx.android.synthetic.main.generic_error_retry_only_layout.*
import kotlinx.android.synthetic.main.no_network_retry_only_layout.*
import javax.inject.Inject

class SubscriptionListFragment : DaggerFragment(), SubscriptionListView {

  @Inject
  lateinit var subscriptionInteract: SubscriptionInteract

  private lateinit var presenter: SubscriptionListPresenter
  private lateinit var activity: SubscriptionView
  private lateinit var activeAdapter: SubscriptionAdapter
  private lateinit var expiredAdapter: SubscriptionAdapter
  private var clickSubject: PublishSubject<String>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    clickSubject = PublishSubject.create()

    presenter =
        SubscriptionListPresenter(this, subscriptionInteract, CompositeDisposable(),
            Schedulers.io(),
            AndroidSchedulers.mainThread())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_subscription_list, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    activeAdapter = SubscriptionAdapter(clickSubject)
    expiredAdapter = SubscriptionAdapter(clickSubject)

    rvActiveSubs.adapter = activeAdapter
    rvActiveSubs.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    rvExpiredSubs.adapter = expiredAdapter
    rvExpiredSubs.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

    presenter.present()
  }


  override fun subscriptionClicks(): Observable<String> = clickSubject!!

  override fun showSubscriptionDetails(packageName: String) {
    activity.showSubscriptionDetails(packageName)
  }

  override fun onActiveSubscriptions(subscriptions: List<SubscriptionItem>) {
    activeAdapter.submitList(subscriptions)
  }

  override fun onExpiredSubscriptions(subscriptions: List<SubscriptionItem>) {
    expiredAdapter.submitList(subscriptions)
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

  override fun onAttach(context: Context) {
    super.onAttach(context)
    require(
        context is SubscriptionView) { SubscriptionListFragment::class.java.simpleName + " needs to be attached to a " + SubscriptionActivity::class.java.simpleName }
    activity = context
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
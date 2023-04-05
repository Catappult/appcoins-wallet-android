package com.asfoundation.wallet.subscriptions.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentSubscriptionListBinding
import com.asfoundation.wallet.subscriptions.SubscriptionAdapter
import com.asfoundation.wallet.subscriptions.SubscriptionItem
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@AndroidEntryPoint
class SubscriptionListFragment : BasePageViewFragment(), SubscriptionListView {

  @Inject
  lateinit var presenter: SubscriptionListPresenter
  private lateinit var activeAdapter: SubscriptionAdapter
  private lateinit var expiredAdapter: SubscriptionAdapter
  private var clickSubject: PublishSubject<Pair<SubscriptionItem, View>>? = null

  private var _binding: FragmentSubscriptionListBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  // fragment_subscription_list.xml
  private val rvActiveSubs get() = binding.rvActiveSubs
  private val rvExpiredSubs get() = binding.rvExpiredSubs
  private val active_title get() = binding.activeTitle
  private val expired_title get() = binding.expiredTitle
  private val main_layout get() = binding.mainLayout
  private val generic_error_retry_only_layout get() = binding.genericErrorRetryOnlyLayout.root
  private val loading_animation get() = binding.loadingAnimation
  private val no_network_retry_only_layout get() = binding.noNetworkRetryOnlyLayout.root
  private val layout_no_subscriptions get() = binding.layoutNoSubscriptions.root

  // generic_error_retry_only_layout.xml
  private val generic_retry_animation get() = binding.genericErrorRetryOnlyLayout.genericRetryAnimation
  private val generic_retry_button get() = binding.genericErrorRetryOnlyLayout.genericRetryButton

  // no_network_retry_only_layout.xml
  private val retry_animation get() = binding.noNetworkRetryOnlyLayout.retryAnimation
  private val retry_button get() = binding.noNetworkRetryOnlyLayout.retryButton

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    clickSubject = PublishSubject.create()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    _binding = FragmentSubscriptionListBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    handleReturnTransition()

    activity?.title = getString(R.string.subscriptions_settings_title)
    activeAdapter = SubscriptionAdapter(clickSubject)
    expiredAdapter = SubscriptionAdapter(clickSubject)

    rvActiveSubs.adapter = activeAdapter
    rvActiveSubs.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    rvExpiredSubs.adapter = expiredAdapter
    rvExpiredSubs.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

    presenter.present()
  }

  private fun handleReturnTransition() {
    postponeEnterTransition()
    rvActiveSubs.post { startPostponedEnterTransition() }
    rvExpiredSubs.post { startPostponedEnterTransition() }
  }

  override fun subscriptionClicks(): Observable<Pair<SubscriptionItem, View>> = clickSubject!!

  override fun hasItems(): Boolean {
    return activeAdapter.itemCount + expiredAdapter.itemCount != 0
  }

  override fun onActiveSubscriptions(subscriptionModels: List<SubscriptionItem>) {
    activeAdapter.submitList(subscriptionModels)
    if (subscriptionModels.isEmpty()) active_title.visibility = View.GONE
    else active_title.visibility = View.VISIBLE
  }

  override fun onExpiredSubscriptions(subscriptionModels: List<SubscriptionItem>) {
    expiredAdapter.submitList(subscriptionModels)
    if (subscriptionModels.isEmpty()) expired_title.visibility = View.GONE
    else expired_title.visibility = View.VISIBLE
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
    _binding = null
  }

  companion object {

    const val FRESH_RELOAD_KEY = "fresh_reload"

    fun newInstance(freshReload: Boolean = false): SubscriptionListFragment {
      return SubscriptionListFragment()
          .apply {
            arguments = Bundle().apply {
              putBoolean(FRESH_RELOAD_KEY, freshReload)
            }
          }
    }
  }
}
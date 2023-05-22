package com.asfoundation.wallet.subscriptions.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentSubscriptionListBinding
import com.asfoundation.wallet.subscriptions.SubscriptionAdapter
import com.asfoundation.wallet.subscriptions.SubscriptionItem
import com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@AndroidEntryPoint
class SubscriptionListFragment : com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment(null), SubscriptionListView {

  @Inject
  lateinit var presenter: SubscriptionListPresenter
  private lateinit var activeAdapter: SubscriptionAdapter
  private lateinit var expiredAdapter: SubscriptionAdapter
  private var clickSubject: PublishSubject<Pair<SubscriptionItem, View>>? = null

  private val binding by viewBinding(FragmentSubscriptionListBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    clickSubject = PublishSubject.create()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = FragmentSubscriptionListBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    handleReturnTransition()

    activity?.title = getString(R.string.subscriptions_settings_title)
    activeAdapter = SubscriptionAdapter(clickSubject)
    expiredAdapter = SubscriptionAdapter(clickSubject)

    binding.rvActiveSubs.adapter = activeAdapter
    binding.rvActiveSubs.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    binding.rvExpiredSubs.adapter = expiredAdapter
    binding.rvExpiredSubs.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

    presenter.present()
  }

  private fun handleReturnTransition() {
    postponeEnterTransition()
    binding.rvActiveSubs.post { startPostponedEnterTransition() }
    binding.rvExpiredSubs.post { startPostponedEnterTransition() }
  }

  override fun subscriptionClicks(): Observable<Pair<SubscriptionItem, View>> = clickSubject!!

  override fun hasItems(): Boolean {
    return activeAdapter.itemCount + expiredAdapter.itemCount != 0
  }

  override fun onActiveSubscriptions(subscriptionModels: List<SubscriptionItem>) {
    activeAdapter.submitList(subscriptionModels)
    if (subscriptionModels.isEmpty()) binding.activeTitle.visibility = View.GONE
    else binding.activeTitle.visibility = View.VISIBLE
  }

  override fun onExpiredSubscriptions(subscriptionModels: List<SubscriptionItem>) {
    expiredAdapter.submitList(subscriptionModels)
    if (subscriptionModels.isEmpty()) binding.expiredTitle.visibility = View.GONE
    else binding.expiredTitle.visibility = View.VISIBLE
  }

  override fun showNoNetworkError() {
    binding.mainLayout.visibility = View.GONE
    binding.noNetworkRetryOnlyLayout.retryAnimation.visibility = View.GONE
    binding.noNetworkRetryOnlyLayout.retryButton.visibility = View.VISIBLE
    binding.genericErrorRetryOnlyLayout.genericRetryAnimation.visibility = View.GONE
    binding.genericErrorRetryOnlyLayout.root.visibility = View.GONE
    binding.loadingAnimation.visibility = View.GONE
    binding.noNetworkRetryOnlyLayout.root.visibility = View.VISIBLE
  }

  override fun showGenericError() {
    binding.mainLayout.visibility = View.GONE
    binding.noNetworkRetryOnlyLayout.retryAnimation.visibility = View.GONE
    binding.genericErrorRetryOnlyLayout.genericRetryAnimation.visibility = View.GONE
    binding.genericErrorRetryOnlyLayout.genericRetryButton.visibility = View.VISIBLE
    binding.loadingAnimation.visibility = View.GONE
    binding.noNetworkRetryOnlyLayout.root.visibility = View.GONE
    binding.genericErrorRetryOnlyLayout.root.visibility = View.VISIBLE
  }

  override fun showSubscriptions() {
    binding.loadingAnimation.visibility = View.GONE
    binding.noNetworkRetryOnlyLayout.root.visibility = View.GONE
    binding.genericErrorRetryOnlyLayout.root.visibility = View.GONE
    binding.layoutNoSubscriptions.root.visibility = View.GONE
    binding.mainLayout.visibility = View.VISIBLE
  }

  override fun showNoSubscriptions() {
    binding.loadingAnimation.visibility = View.GONE
    binding.mainLayout.visibility = View.GONE
    binding.genericErrorRetryOnlyLayout.root.visibility = View.GONE
    binding.noNetworkRetryOnlyLayout.root.visibility = View.GONE
    binding.layoutNoSubscriptions.root.visibility = View.VISIBLE
  }

  override fun showLoading() {
    binding.mainLayout.visibility = View.GONE
    binding.noNetworkRetryOnlyLayout.root.visibility = View.GONE
    binding.genericErrorRetryOnlyLayout.root.visibility = View.GONE
    binding.layoutNoSubscriptions.root.visibility = View.GONE
    binding.loadingAnimation.visibility = View.VISIBLE
  }

  override fun retryClick() = RxView.clicks(binding.noNetworkRetryOnlyLayout.retryButton)

  override fun getRetryGenericClicks() = RxView.clicks(binding.genericErrorRetryOnlyLayout.genericRetryButton)

  override fun getRetryNetworkClicks() = RxView.clicks(binding.noNetworkRetryOnlyLayout.retryButton)

  override fun showNoNetworkRetryAnimation() {
    binding.noNetworkRetryOnlyLayout.retryButton.visibility = View.INVISIBLE
    binding.noNetworkRetryOnlyLayout.retryAnimation.visibility = View.VISIBLE
  }

  override fun showGenericRetryAnimation() {
    binding.genericErrorRetryOnlyLayout.genericRetryButton.visibility = View.INVISIBLE
    binding.genericErrorRetryOnlyLayout.genericRetryAnimation.visibility = View.VISIBLE
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
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
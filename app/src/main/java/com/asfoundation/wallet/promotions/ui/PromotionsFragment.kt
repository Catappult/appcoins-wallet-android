package com.asfoundation.wallet.promotions.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentPromotionsBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.promotions.model.Promotion
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.asfoundation.wallet.promotions.ui.list.PromotionsAdapter
import com.asfoundation.wallet.ui.widget.MarginItemDecoration
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import javax.inject.Inject

class PromotionsFragment : BasePageViewFragment(),
    SingleStateFragment<PromotionsState, PromotionsSideEffect> {
  @Inject
  lateinit var promotionsViewModelFactory: PromotionsViewModelFactory

  @Inject
  lateinit var navigator: PromotionsNavigator

  private lateinit var adapter: PromotionsAdapter

  private val viewModel: PromotionsViewModel by viewModels { promotionsViewModelFactory }
  private val views by viewBinding(FragmentPromotionsBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_promotions, container, false)
  }

  override fun onResume() {
    super.onResume()
    viewModel.fetchPromotions()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
//    adapter = PromotionsAdapter(emptyList()) { promotionClick ->
//      viewModel.promotionClicked(promotionClick)
//    }
    views.rvPromotions.adapter = adapter
    views.rvPromotions.addItemDecoration(
        MarginItemDecoration(resources.getDimension(R.dimen.promotions_item_margin)
            .toInt()))
//    views.gamificationInfoBtn.setOnClickListener { viewModel.gamificationInfoClicked() }
    views.noNetwork.retryButton.setOnClickListener { viewModel.fetchPromotions() }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onStateChanged(state: PromotionsState) {
    setPromotionsModel(state.promotionsModelAsync)
  }

  override fun onSideEffect(sideEffect: PromotionsSideEffect) {
    when (sideEffect) {
      is PromotionsSideEffect.NavigateToGamification -> navigator.navigateToGamification(
          sideEffect.cachedBonus)
      is PromotionsSideEffect.NavigateToShare -> navigator.handleShare(sideEffect.url)
      PromotionsSideEffect.NavigateToInfo -> navigator.navigateToInfo()
      PromotionsSideEffect.NavigateToInviteFriends -> navigator.navigateToInviteFriends()
      PromotionsSideEffect.ShowErrorToast -> showErrorToast()
    }
  }

  private fun setPromotionsModel(asyncPromotionsModel: Async<PromotionsModel>) {
    when (asyncPromotionsModel) {
      Async.Uninitialized,
      is Async.Loading -> {
        if (asyncPromotionsModel.value == null) {
          showLoading()
        } else {
          if (asyncPromotionsModel.value.error == PromotionsModel.Status.NO_NETWORK) {
            showNoNetworkErrorLoading()
          }
        }
      }
      is Async.Fail -> showErrorToast()
      // PromotionsModel has the errors mapped into the object itself, so expected errors will be
      // on success... In the future errors should just be thrown so they're handled with Async.Fail
      is Async.Success -> {
        setPromotions(asyncPromotionsModel(), asyncPromotionsModel.previousValue)
      }
    }
  }

  private fun setPromotions(promotionsModel: PromotionsModel, previousModel: PromotionsModel?) {
    hideLoading()
//    if (promotionsModel.hasError() && !promotionsModel.fromCache) {
//      // In the future we should get rid of this previousModel. Here it exists because the offline
//      // first flow emits when it shouldn't (e.g. it emits a network error even if local data exists)
//      if (previousModel == null || previousModel.hasError()) {
//        if (promotionsModel.error == PromotionsModel.Status.NO_NETWORK) {
//          showNetworkErrorView()
//        } else {
//          // In case of error that is not "no network", this screen will be shown. This was already
//          // like this. I think a general error screen was implemented with vouchers, so on merge
//          // we should check this out.
//          showNoPromotionsScreen()
//        }
//      }
//    } else if (promotionsModel.walletOrigin == PromotionsModel.WalletOrigin.UNKNOWN) {
//      if (!promotionsModel.fromCache) {
//        showLockedPromotionsScreen()
//      }
//    } else {
//      if (promotionsModel.promotions.isEmpty()) {
//        showNoPromotionsScreen()
//      } else {
//        showPromotions(promotionsModel.promotions)
//      }
//    }
  }

  private fun showPromotions(promotions: List<Promotion>) {
    adapter.setPromotions(promotions)
    views.rvPromotions.visibility = View.VISIBLE
    views.noNetwork.root.visibility = View.GONE
    views.lockedPromotions.root.visibility = View.GONE
    views.noPromotions.root.visibility = View.GONE
  }

  private fun showNoPromotionsScreen() {
    views.noNetwork.root.visibility = View.GONE
    views.noNetwork.retryAnimation.visibility = View.GONE
    views.noPromotions.root.visibility = View.VISIBLE
    views.lockedPromotions.root.visibility = View.GONE
  }

  private fun showLoading() {
    views.promotionsProgressBar.visibility = View.VISIBLE
    views.lockedPromotions.root.visibility = View.GONE
  }

  private fun showNoNetworkErrorLoading() {
    views.noNetwork.retryButton.visibility = View.INVISIBLE
    views.noNetwork.retryAnimation.visibility = View.VISIBLE
  }

  private fun hideLoading() {
    views.promotionsProgressBar.visibility = View.GONE
  }

  private fun showErrorToast() {
    Toast.makeText(requireContext(), R.string.unknown_error, Toast.LENGTH_SHORT)
        .show()
  }

  private fun showNetworkErrorView() {
    views.rvPromotions.visibility = View.GONE
    views.noPromotions.root.visibility = View.GONE
    views.noNetwork.root.visibility = View.VISIBLE
    views.noNetwork.retryButton.visibility = View.VISIBLE
    views.noNetwork.retryAnimation.visibility = View.GONE
  }

  private fun showLockedPromotionsScreen() {
    views.noNetwork.root.visibility = View.GONE
    views.noNetwork.retryAnimation.visibility = View.GONE
    views.noPromotions.root.visibility = View.GONE
    views.lockedPromotions.root.visibility = View.VISIBLE
  }
}
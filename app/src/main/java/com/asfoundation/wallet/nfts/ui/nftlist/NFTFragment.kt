package com.asfoundation.wallet.nfts.ui.nftlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.asf.wallet.databinding.FragmentNftsBinding
import com.asfoundation.wallet.nfts.domain.NFTItem
import com.asfoundation.wallet.nfts.list.NFTsController
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NFTFragment : BasePageViewFragment(),
  SingleStateFragment<NFTState, NFTSideEffect> {


  @Inject
  lateinit var navigator: NFTNavigator

  private lateinit var nftsController: NFTsController

  private val viewModel: NFTViewModel by viewModels()
  private val views by viewBinding(FragmentNftsBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = FragmentNftsBinding.inflate(inflater).root

  override fun onResume() {
    super.onResume()
    viewModel.fetchNFTList()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    nftsController = NFTsController()
    setListeners()
    views.rvNfts.setController(nftsController)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }


  override fun onStateChanged(state: NFTState) {
    setNFTItem(state.nftListModelAsync)
  }

  override fun onSideEffect(sideEffect: NFTSideEffect) {
    when (sideEffect) {
      is NFTSideEffect.NavigateToInfo -> navigator.navigateToInfo(sideEffect.nftData)

    }
  }

  private fun setListeners() {
    views.actionBack.setOnClickListener { navigator.navigateBack() }
    nftsController.clickListener = { nftClick ->
      viewModel.nftClicked(nftClick)
    }
    views.refreshLayout.setOnRefreshListener { viewModel.fetchNFTList() }
    views.noNetwork.retryButton.setOnClickListener { networkRetry() }
  }

  private fun setNFTItem(asyncNFTListModel: Async<List<NFTItem>>) {
    when (asyncNFTListModel) {
      Async.Uninitialized, is Async.Loading -> {
        if (asyncNFTListModel.value == null) {
          showLoading()
        }
      }
      is Async.Fail -> {
        hideLoading()
        showNetworkErrorView()
      }
      is Async.Success -> {
        setNFTs(asyncNFTListModel())
      }
    }
  }

  private fun setNFTs(nftListModel: List<NFTItem>) {
    if (nftListModel.isEmpty()) {
      showNoNFTsView()
    } else {
      showNFTs(nftListModel)
    }
  }

  private fun showNFTs(nftListModel: List<NFTItem>) {
    hideLoading()
    hideNetworkErrorView()
    hideNoNFTsView()
    nftsController.setData(nftListModel)
    views.rvNfts.visibility = View.VISIBLE
  }

  private fun hideNFTs() {
    views.rvNfts.visibility = View.GONE
  }

  private fun showLoading() {
    hideNetworkErrorView()
    hideNFTs()
    hideNoNFTsView()
    views.nftsProgressBar.visibility = View.VISIBLE
  }

  private fun hideLoading() {
    views.refreshLayout.isRefreshing = false
    views.nftsProgressBar.visibility = View.GONE
  }

  private fun networkRetry() {
    viewModel.fetchNFTList()
    views.noNetwork.retryAnimation.visibility = View.GONE
    views.noNetwork.retryAnimation.visibility = View.VISIBLE
    views.noNetwork.retryButton.visibility = View.INVISIBLE
  }

  private fun showNetworkErrorView() {
    hideNFTs()
    hideLoading()
    hideNoNFTsView()
    views.noNetwork.root.visibility = View.VISIBLE
    views.noNetwork.retryButton.visibility = View.VISIBLE
    views.noNetwork.retryAnimation.visibility = View.GONE
  }

  private fun hideNetworkErrorView() {
    views.noNetwork.root.visibility = View.GONE
    views.noNetwork.retryButton.visibility = View.GONE
    views.noNetwork.retryAnimation.visibility = View.GONE
  }

  private fun showNoNFTsView() {
    hideLoading()
    hideNFTs()
    hideNetworkErrorView()
    views.noNfts.root.visibility = View.VISIBLE
  }

  private fun hideNoNFTsView() {
    views.noNfts.root.visibility = View.GONE
  }
}
package com.asfoundation.wallet.nfts.ui.nftlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentNftsBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.nfts.domain.NFTItem
import com.asfoundation.wallet.nfts.list.NFTsController
import com.asfoundation.wallet.ui.widget.MarginItemDecoration
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import javax.inject.Inject

class NFTFragment : BasePageViewFragment() ,
  SingleStateFragment<NFTState, NFTSideEffect> {

  @Inject
  lateinit var viewModelFactory: NFTViewModelFactory

  @Inject
  lateinit var navigator: NFTNavigator

  private lateinit var nftsController: NFTsController

  private val viewModel: NFTViewModel by viewModels { viewModelFactory }
  private val views by viewBinding(FragmentNftsBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_nfts, container, false)
  }

  override fun onResume() {
    super.onResume()
    viewModel.fetchNFTList()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    nftsController = NFTsController()
    nftsController.clickListener = { nftClick ->
      viewModel.nftClicked(nftClick)
    }
    views.rvNfts.setController(nftsController)
    //TO-Change
    views.rvNfts.addItemDecoration(
      MarginItemDecoration(resources.getDimension(R.dimen.promotions_item_margin)
        .toInt())
    )

    views.noNetwork.retryButton.setOnClickListener { viewModel.fetchNFTList() }
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

  private fun setNFTItem(asyncNFTListModel: Async<List<NFTItem>>) {
    when (asyncNFTListModel) {
      Async.Uninitialized,
      is Async.Loading -> {
        if (asyncNFTListModel.value == null) {
          showLoading()

        }
      }
      is Async.Fail -> showErrorToast()
      is Async.Success -> {
        setNFTs(asyncNFTListModel())
      }
    }
  }

  private fun setNFTs(nftListModel: List<NFTItem>) {
    hideLoading()
    showNFTs(nftListModel)
  }

  private fun showNFTs(nftListModel: List<NFTItem>) {
    nftsController.setData(nftListModel)
  }

  private fun showLoading() {
    views.nftsProgressBar.visibility = View.VISIBLE
  }

  private fun hideLoading() {
    views.nftsProgressBar.visibility = View.GONE
  }

  private fun showErrorToast() {
    Toast.makeText(requireContext(), R.string.unknown_error, Toast.LENGTH_SHORT)
      .show()
  }

  private fun showNoNFTsScreen() {
    views.noNetwork.root.visibility = View.GONE
    views.noNetwork.retryAnimation.visibility = View.GONE
    views.noNfts.root.visibility = View.VISIBLE
  }


}
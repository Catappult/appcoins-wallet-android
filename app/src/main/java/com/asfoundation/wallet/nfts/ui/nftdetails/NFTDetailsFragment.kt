package com.asfoundation.wallet.nfts.ui.nftdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentNftBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.bumptech.glide.Glide
import javax.inject.Inject

class NFTDetailsFragment : BasePageViewFragment() ,
  SingleStateFragment<NFTDetailsState, NFTDetailsSideEffect> {

  @Inject
  lateinit var viewModelFactory: NFTDetailsViewModelFactory

  @Inject
  lateinit var navigator: NFTDetailsNavigator

  private val viewModel: NFTDetailsViewModel by viewModels { viewModelFactory }
  private val views by viewBinding(FragmentNftBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_nft, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.nftTitle.text = viewModel.state.data.name
    views.nftSubtitle.text = viewModel.state.data.description
    Glide.with(views.root).load(viewModel.state.data.imageURL).into(views.nftImage)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onSideEffect(sideEffect: NFTDetailsSideEffect) = Unit

  override fun onStateChanged(state: NFTDetailsState) = Unit

  companion object {
    internal const val NFTITEMDATA = "data"
  }


}






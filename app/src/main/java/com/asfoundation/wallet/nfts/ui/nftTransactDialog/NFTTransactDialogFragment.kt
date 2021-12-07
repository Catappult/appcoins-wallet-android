package com.asfoundation.wallet.nfts.ui.nftTransactDialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentNftTransactBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.di.DaggerBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import javax.inject.Inject

class NFTTransactDialogFragment : DaggerBottomSheetDialogFragment(),
    SingleStateFragment<NFTTransactState, NFTTransactSideEffect> {

  @Inject
  lateinit var viewModelFactory: NFTTransactDialogViewModelFactory

  private val viewModel: NFTTransactDialogViewModel by viewModels { viewModelFactory }
  private val views by viewBinding(FragmentNftTransactBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_nft_transact, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    setListeners()
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

  override fun onStateChanged(state: NFTTransactState) {
    views.title.text = state.data.name
  }

  override fun onSideEffect(sideEffect: NFTTransactSideEffect) = Unit

  companion object {
    internal const val NFT_ITEM_DATA = "data"
  }

  private fun setListeners() {
    views.sendButton.setOnClickListener {
      Log.d("NFT", "botao"); viewModel.estimateGas(views.sendToAddress.text.toString())
    }
  }
}
package com.asfoundation.wallet.nfts.ui.nftTransactDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentNftTransactBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.di.DaggerBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.math.BigInteger
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
    views.layoutNftTransactDone.doneMensage.text =
        getString(R.string.nfts_transact_done_mensage, state.data.name)

    when (val transactionHash = state.transactionHashAsync) {
      is Async.Fail -> showError(getString(R.string.nfts_generic_error))
      is Async.Loading -> showLoading()
      is Async.Success -> if (transactionHash.value!!.startsWith(
              "0x")) showSuccess() else showError(transactionHash.value!!)
      Async.Uninitialized -> setGasPrice(state.gasPriceAsync)
    }
  }

  private fun setGasPrice(gasPriceAsync: Async<Pair<BigInteger, BigInteger>>) {
    when (gasPriceAsync) {
      is Async.Fail -> showError(getString(R.string.nfts_generic_error))
      is Async.Loading -> showLoading()
      is Async.Success -> viewModel.send(views.layoutNftTransactEntry.sendToAddress.text.toString(),
          gasPriceAsync().first, gasPriceAsync().second)
      Async.Uninitialized -> Unit
    }
  }

  private fun showLoading() {
    views.layoutNftTransactEntry.root.visibility = View.INVISIBLE
    views.layoutNftTransactDone.root.visibility = View.GONE
    views.layoutNftTransactLoading.root.visibility = View.VISIBLE
  }

  private fun showSuccess() {
    views.layoutNftTransactEntry.root.visibility = View.INVISIBLE
    views.layoutNftTransactLoading.root.visibility = View.INVISIBLE
    views.layoutNftTransactDone.errorAnimation.visibility = View.GONE
    views.layoutNftTransactDone.successAnimation.visibility = View.VISIBLE
    views.layoutNftTransactDone.root.visibility = View.VISIBLE
  }

  private fun showError(errorMensage: String) {
    views.layoutNftTransactEntry.root.visibility = View.INVISIBLE
    views.layoutNftTransactLoading.root.visibility = View.INVISIBLE
    views.layoutNftTransactDone.successAnimation.visibility = View.INVISIBLE
    views.layoutNftTransactDone.errorAnimation.visibility = View.VISIBLE
    views.layoutNftTransactDone.doneMensage.text = errorMensage.capitalize()
    views.layoutNftTransactDone.root.visibility = View.VISIBLE
  }

  override fun onSideEffect(sideEffect: NFTTransactSideEffect) = Unit

  companion object {
    internal const val NFT_ITEM_DATA = "data"
  }

  private fun setListeners() {
    views.layoutNftTransactEntry.sendButton.setOnClickListener {
      viewModel.estimateGas(views.layoutNftTransactEntry.sendToAddress.text.toString())
    }

    views.layoutNftTransactDone.doneButton.setOnClickListener {
      dismiss()
    }
  }
}
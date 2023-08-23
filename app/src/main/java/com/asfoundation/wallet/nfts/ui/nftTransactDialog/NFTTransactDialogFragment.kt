package com.asfoundation.wallet.nfts.ui.nftTransactDialog

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.utils.android_common.BalanceUtils
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentNftTransactBinding
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.asfoundation.wallet.nfts.domain.FailedNftTransfer
import com.asfoundation.wallet.nfts.domain.GasInfo
import com.asfoundation.wallet.nfts.domain.NftTransferResult
import com.asfoundation.wallet.nfts.domain.SuccessfulNftTransfer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigInteger
import java.math.RoundingMode
import javax.inject.Inject

@AndroidEntryPoint
class NFTTransactDialogFragment : BottomSheetDialogFragment(),
  SingleStateFragment<NFTTransactState, NFTTransactSideEffect> {

  @Inject
  lateinit var viewModelFactory: NFTTransactDialogViewModelFactory

  private val viewModel: NFTTransactDialogViewModel by viewModels { viewModelFactory }
  private val views by viewBinding(FragmentNftTransactBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = FragmentNftTransactBinding.inflate(inflater).root

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

  override fun getTheme(): Int = R.style.AppBottomSheetDialogThemeDraggable

  override fun onStateChanged(state: NFTTransactState) {

    when (val transactionResult = state.transactionResultAsync) {
      is Async.Uninitialized -> setGasPrice(state.gasPriceAsync)
      is Async.Loading -> showLoading()
      is Async.Fail -> showError(getString(R.string.nfts_generic_error))
      is Async.Success -> transactionResult.value?.let { showResult(it, state.data.name ?: "NFT") }
    }
  }

  private fun showResult(result: NftTransferResult, nftName: String) {
    when (result) {
      is SuccessfulNftTransfer -> showSuccess(nftName)
      is FailedNftTransfer.AlreadyKnown -> showError(
          getString(R.string.nfts_transact_error_already_in_progress))
      is FailedNftTransfer.ReplacementUnderpriced -> showError(
          getString(R.string.nfts_transact_error_already_in_progress))
      is FailedNftTransfer.InsufficientFunds -> showError(
          getString(R.string.nfts_transact_error_no_funds))
      is FailedNftTransfer.GasToLow -> showError(getString(R.string.nfts_transact_error_low_gas))
      else -> showError(getString(R.string.nfts_generic_error))
    }
  }

  private fun setGasPrice(gasInfoAsync: Async<GasInfo>) {
    when (gasInfoAsync) {
      is Async.Uninitialized -> Unit
      is Async.Loading -> showLoading()
      is Async.Fail -> showError(getString(R.string.nfts_generic_error))
      is Async.Success -> showPickGas(gasInfoAsync())
    }
  }

  private fun showPickGas(gasInfo: GasInfo) {
    views.layoutNftTransactEntry.root.visibility = View.INVISIBLE
    views.layoutNftTransactDone.root.visibility = View.GONE
    views.layoutNftTransactLoading.root.visibility = View.GONE
    views.layoutNftTransactPickGas.gasPriceInput.text = Editable.Factory.getInstance()
        .newEditable(gasInfo.gasPrice.toString())
    views.layoutNftTransactPickGas.gasPriceInput.doAfterTextChanged {
      updateFee(gasInfo.copyWith(
          gasPrice = views.layoutNftTransactPickGas.gasPriceInput.text.toString()
              .toBigIntegerOrNull() ?: BigInteger.ZERO,
          gasLimit = views.layoutNftTransactPickGas.gasLimitInput.text.toString()
              .toBigIntegerOrNull() ?: BigInteger.ZERO))
    }
    views.layoutNftTransactPickGas.gasLimitInput.text = Editable.Factory.getInstance()
        .newEditable(gasInfo.gasLimit.toString())
    views.layoutNftTransactPickGas.gasLimitInput.doAfterTextChanged {
      updateFee(gasInfo.copyWith(
          gasPrice = views.layoutNftTransactPickGas.gasPriceInput.text.toString()
              .toBigIntegerOrNull() ?: BigInteger.ZERO,
          gasLimit = views.layoutNftTransactPickGas.gasLimitInput.text.toString()
              .toBigIntegerOrNull() ?: BigInteger.ZERO))
    }
    updateFee(gasInfo)
    views.layoutNftTransactPickGas.root.visibility = View.VISIBLE
  }

  private fun updateFee(gasInfo: GasInfo) {
    val fiat = gasInfo.symbol + (BalanceUtils.weiToEth(
        (gasInfo.gasPrice * gasInfo.gasLimit).toBigDecimal()) * gasInfo.rate).setScale(4,
        RoundingMode.CEILING) + " " + gasInfo.currency
    val eth = BalanceUtils.weiToEth((gasInfo.gasPrice * gasInfo.gasLimit).toBigDecimal())
        .setScale(6, RoundingMode.FLOOR)
        .toString() + " ETH"

    views.layoutNftTransactPickGas.priceFiat.text = fiat
    views.layoutNftTransactPickGas.priceEth.text = eth
  }


  private fun showLoading() {
    views.layoutNftTransactEntry.root.visibility = View.INVISIBLE
    views.layoutNftTransactDone.root.visibility = View.GONE
    views.layoutNftTransactPickGas.root.visibility = View.GONE
    views.layoutNftTransactLoading.root.visibility = View.VISIBLE
  }

  private fun showSuccess(nftName: String) {

    views.layoutNftTransactDone.doneMensage.text =
        String.format(getString(R.string.nfts_transact_done_mensage), nftName)
    views.layoutNftTransactEntry.root.visibility = View.INVISIBLE
    views.layoutNftTransactLoading.root.visibility = View.INVISIBLE
    views.layoutNftTransactPickGas.root.visibility = View.GONE
    views.layoutNftTransactDone.errorAnimation.visibility = View.GONE
    views.layoutNftTransactDone.successAnimation.visibility = View.VISIBLE
    views.layoutNftTransactDone.root.visibility = View.VISIBLE
  }

  private fun showError(errorMessage: String) {
    views.layoutNftTransactEntry.root.visibility = View.INVISIBLE
    views.layoutNftTransactLoading.root.visibility = View.INVISIBLE
    views.layoutNftTransactPickGas.root.visibility = View.GONE
    views.layoutNftTransactDone.successAnimation.visibility = View.INVISIBLE
    views.layoutNftTransactDone.errorAnimation.visibility = View.VISIBLE
    views.layoutNftTransactDone.doneMensage.text = errorMessage
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

    views.layoutNftTransactPickGas.sendButton.setOnClickListener {
      viewModel.send(views.layoutNftTransactEntry.sendToAddress.text.toString(),
          BigInteger(views.layoutNftTransactPickGas.gasPriceInput.text.toString()),
          BigInteger(views.layoutNftTransactPickGas.gasLimitInput.text.toString()))
    }
  }
}
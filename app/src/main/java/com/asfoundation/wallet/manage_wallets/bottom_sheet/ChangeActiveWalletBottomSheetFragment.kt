package com.asfoundation.wallet.manage_wallets.bottom_sheet


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.utils.android_common.AmountUtils.formatMoney
import com.asf.wallet.R
import com.asf.wallet.databinding.ChangeActiveWalletBottomSheetLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangeActiveWalletBottomSheetFragment : BottomSheetDialogFragment(),
  SingleStateFragment<ChangeActiveWalletBottomSheetState, ChangeActiveWalletBottomSheetSideEffect> {

  @Inject
  lateinit var navigator: ChangeActiveWalletBottomSheetNavigator

  private val viewModel: ChangeActiveWalletBottomSheetViewModel by viewModels()
  private val views by viewBinding(ChangeActiveWalletBottomSheetLayoutBinding::bind)

  private val manageWalletSharedViewModel: ManageWalletSharedViewModel by activityViewModels()

  companion object {
    const val WALLET_NAME = "wallet_name"
    const val WALLET_ADDRESS = "wallet_address"
    const val WALLET_BALANCE = "wallet_balance"
    const val WALLET_BALANCE_SYMBOL = "wallet_balance_symbol"


    @JvmStatic
    fun newInstance(): ChangeActiveWalletBottomSheetFragment {
      return ChangeActiveWalletBottomSheetFragment()
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = ChangeActiveWalletBottomSheetLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val walletBalance = arguments?.getString(WALLET_BALANCE)
    val walletBalanceSymbol = arguments?.getString(WALLET_BALANCE_SYMBOL)
    val walletAddress = arguments?.getString(WALLET_ADDRESS)
    val walletName = arguments?.getString(WALLET_NAME)
    if (!walletName.isNullOrEmpty()) {
      views.newWalletName.text = walletName
    }
    if (!walletBalance.isNullOrEmpty() && !walletBalanceSymbol.isNullOrEmpty()) {
      views.newWalletBalance.text = walletBalance.formatMoney(walletBalanceSymbol, "")
    }
    setListeners(walletAddress)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int {
    return R.style.AppBottomSheetDialogThemeDraggable
  }

  private fun setListeners(walletAddress: String?) {
    views.manageWalletBottomSheetSubmitButton.setOnClickListener {
      showLoading()
      walletAddress?.let { it1 -> viewModel.changeActiveWallet(it1) }
    }
  }

  override fun onStateChanged(state: ChangeActiveWalletBottomSheetState) {
  }

  private fun showLoading() {
    hideAll()
    views.manageWalletBottomSheetSystemView.visibility = View.VISIBLE
    views.manageWalletBottomSheetSystemView.showProgress(true)
  }

  private fun hideAll() {
    views.walletNewNameSheetLayout.visibility = View.GONE
    views.manageWalletBottomSheetTitle.visibility = View.GONE
    views.manageWalletBottomSheetSubmitButton.visibility = View.GONE
  }

  override fun onSideEffect(sideEffect: ChangeActiveWalletBottomSheetSideEffect) {
    when (sideEffect) {
      is ChangeActiveWalletBottomSheetSideEffect.NavigateBack -> {
        navigator.navigateBack()
      }

      is ChangeActiveWalletBottomSheetSideEffect.WalletChanged -> {
        manageWalletSharedViewModel.onBottomSheetDismissed()
        navigator.navigateBack()
      }
    }
  }

}
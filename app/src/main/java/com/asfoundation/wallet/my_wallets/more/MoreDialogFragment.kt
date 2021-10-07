package com.asfoundation.wallet.my_wallets.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentMyWalletsMoreBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.di.DaggerBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import javax.inject.Inject

class MoreDialogFragment : DaggerBottomSheetDialogFragment(),
    SingleStateFragment<MoreDialogState, MoreDialogSideEffect> {

  @Inject
  lateinit var viewModelFactory: MoreDialogViewModelFactory

  @Inject
  lateinit var navigator: MoreDialogNavigator

  private val viewModel: MoreDialogViewModel by viewModels { viewModelFactory }
  private val views by viewBinding(FragmentMyWalletsMoreBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_my_wallets_more, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setListeners()
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

  override fun onStateChanged(state: MoreDialogState) {
    views.deleteWalletCardView.visibility = if (state.showDeleteWallet) View.VISIBLE else View.GONE
    views.verifyCardCardView.visibility = if (state.showVerifyCard) View.VISIBLE else View.GONE
  }

  override fun onSideEffect(sideEffect: MoreDialogSideEffect) = Unit

  private fun setListeners() {
    views.newWalletCardView.setOnClickListener { navigator.navigateToCreateNewWallet() }
    views.recoverWalletCardView.setOnClickListener { navigator.navigateToRestoreWallet() }
    views.backupWalletCardView.setOnClickListener {
      navigator.navigateToBackupWallet(viewModel.state.walletAddress)
    }
    views.verifyCardCardView.setOnClickListener { navigator.navigateToVerify() }
    views.deleteWalletCardView.setOnClickListener {
      navigator.navigateToRemoveWallet(viewModel.state.walletAddress,
          viewModel.state.totalFiatBalance,
          viewModel.state.appcoinsBalance, viewModel.state.creditsBalance,
          viewModel.state.ethereumBalance)
    }
  }

  companion object {
    internal const val WALLET_ADDRESS_KEY = "wallet_address"
    internal const val FIAT_BALANCE_KEY = "fiat_balance"
    internal const val APPC_BALANCE_KEY = "appc_balance"
    internal const val CREDITS_BALANCE_KEY = "credits_balance"
    internal const val ETHEREUM_BALANCE_KEY = "ethereum_balance"
    internal const val SHOW_VERIFY_CARD_KEY = "show_verify_card"
    internal const val SHOW_DELETE_WALLET_KEY = "show_delete_wallet"
  }
}
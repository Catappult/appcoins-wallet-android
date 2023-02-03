package com.asfoundation.wallet.my_wallets.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentMyWalletsMoreBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MoreDialogFragment : BottomSheetDialogFragment(),
  SingleStateFragment<MoreDialogState, MoreDialogSideEffect> {

  @Inject
  lateinit var navigator: MoreDialogNavigator

  private val viewModel: MoreDialogViewModel by viewModels()
  private val views by viewBinding(FragmentMyWalletsMoreBinding::bind)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? = inflater.inflate(R.layout.fragment_my_wallets_more, container, false)

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

  override fun onResume() {
    super.onResume()
    viewModel.refreshData()
  }

  override fun getTheme(): Int = R.style.AppBottomSheetDialogThemeDraggable

  override fun onStateChanged(state: MoreDialogState) {
    val wallets = state.walletsAsync()
    val lastWallet: Boolean = wallets?.let { it.size > 1 } == false
    views.deleteWalletCardView.visibility = if (lastWallet) View.GONE else View.VISIBLE
    views.walletsView.apply {
      if (wallets.isNullOrEmpty()) {
        removeAllViews()
      } else {
        val difference = wallets.size - childCount
        if (difference < 0) removeViews(childCount, -difference)
        children.toList().zip(wallets).forEach {
          it.second.toWalletItemView(it.first, viewModel::changeActiveWallet)
        }
        if (difference > 0) {
          wallets.listIterator(wallets.size - difference).forEach {
            addView(it.toWalletItemView(context, viewModel::changeActiveWallet))
          }
        }
      }
    }

    views.scrollviewMyWallets.run {
      post { fullScroll(View.FOCUS_DOWN) }
    }
  }

  override fun onSideEffect(sideEffect: MoreDialogSideEffect) {
    when (sideEffect) {
      MoreDialogSideEffect.NavigateBack -> navigator.navigateBack()
    }
  }

  private fun setListeners() {
    views.newWalletCardView.setOnClickListener { navigator.navigateToCreateNewWallet() }
    views.recoverWalletCardView.setOnClickListener { navigator.navigateToRestoreWallet() }
    views.deleteWalletCardView.setOnClickListener {
      navigator.navigateToRemoveWallet(
        viewModel.state.walletAddress,
        viewModel.state.totalFiatBalance,
        viewModel.state.appcoinsBalance,
        viewModel.state.creditsBalance,
        viewModel.state.ethereumBalance
      )
    }
  }

  companion object {
    internal const val WALLET_ADDRESS_KEY = "wallet_address"
    internal const val FIAT_BALANCE_KEY = "fiat_balance"
    internal const val APPC_BALANCE_KEY = "appc_balance"
    internal const val CREDITS_BALANCE_KEY = "credits_balance"
    internal const val ETHEREUM_BALANCE_KEY = "ethereum_balance"
  }
}
package com.asfoundation.wallet.my_wallets.change_wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentChangeActiveWalletBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.di.DaggerBottomSheetDialogFragment
import com.asfoundation.wallet.ui.wallets.WalletBalance
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.math.BigDecimal
import javax.inject.Inject

class ChangeActiveWalletDialogFragment : DaggerBottomSheetDialogFragment(),
    SingleStateFragment<ChangeActiveWalletState, ChangeActiveWalletSideEffect> {

  @Inject
  lateinit var viewModelFactory: ChangeActiveWalletDialogViewModelFactory

  @Inject
  lateinit var navigator: ChangeActiveWalletDialogNavigator

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  private val viewModel: ChangeActiveWalletDialogViewModel by viewModels { viewModelFactory }
  private val views by viewBinding(FragmentChangeActiveWalletBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_change_active_wallet, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.changeWalletButton.setOnClickListener { viewModel.changeActiveWallet() }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

  override fun onStateChanged(state: ChangeActiveWalletState) {
    when (state.changeWalletAsync) {
      Async.Uninitialized -> setWalletDetails(state.walletBalance)
      is Async.Loading -> setLoading()
      is Async.Fail -> Unit
      is Async.Success -> Unit
    }
  }

  private fun setLoading() {
    views.loading.visibility = View.VISIBLE
    views.addressIcon.visibility = View.INVISIBLE
    views.walletAddressCardView.visibility = View.INVISIBLE
    views.totalBalanceLabelTextView.visibility = View.INVISIBLE
    views.totalBalanceTextView.visibility = View.INVISIBLE
    views.changeWalletButton.isEnabled = false
  }

  private fun setWalletDetails(walletBalance: WalletBalance) {
    views.loading.visibility = View.GONE
    views.addressIcon.visibility = View.VISIBLE
    views.walletAddressTextView.visibility = View.VISIBLE
    views.totalBalanceLabelTextView.visibility = View.VISIBLE
    views.totalBalanceTextView.visibility = View.VISIBLE
    views.changeWalletButton.isEnabled = true

    views.walletAddressTextView.text = walletBalance.walletAddress
    val balance = walletBalance.balance
    val amountText = if (balance.amount.compareTo(BigDecimal("-1")) == 1) {
      formatter.formatCurrency(balance.amount)
    } else {
      "-1"
    }
    val balanceText = balance.symbol + amountText
    views.totalBalanceTextView.text = balanceText
  }

  override fun onSideEffect(sideEffect: ChangeActiveWalletSideEffect) {
    when (sideEffect) {
      ChangeActiveWalletSideEffect.NavigateBack -> navigator.navigateBack()
    }
  }

  companion object {
    internal const val WALLET_BALANCE_KEY = "wallet_balance"
  }

}
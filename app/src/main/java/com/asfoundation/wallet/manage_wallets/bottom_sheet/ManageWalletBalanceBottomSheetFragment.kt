package com.asfoundation.wallet.manage_wallets.bottom_sheet


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.utils.android_common.AmountUtils.formatMoney
import com.asf.wallet.databinding.ManageWalletBalanceBottomSheetLayoutBinding
import com.asfoundation.wallet.manage_wallets.ManageWalletViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ManageWalletBalanceBottomSheetFragment() : BottomSheetDialogFragment(),
  SingleStateFragment<ManageWalletBalanceBottomSheetState, ManageWalletBalanceBottomSheetSideEffect> {


  @Inject
  lateinit var navigator: ManageWalletBalanceBottomSheetNavigator

  private val viewModel: ManageWalletBalanceBottomSheetViewModel by viewModels()
  private val views by viewBinding(ManageWalletBalanceBottomSheetLayoutBinding::bind)

  companion object {

    @JvmStatic
    fun newInstance(): ManageWalletBalanceBottomSheetFragment {
      return ManageWalletBalanceBottomSheetFragment()
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    viewModel.getWallets()
    return ManageWalletBalanceBottomSheetLayoutBinding.inflate(inflater).root
  }

  @SuppressLint("SetTextI18n")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    when (val uiState = viewModel.uiState.value) {
      is ManageWalletViewModel.UiState.Success -> {
        uiState.activeWalletInfo.walletBalance.let {
          views.totalBalanceValue.text = it.creditsOnlyFiat.amount
            .toString()
            .formatMoney(it.creditsOnlyFiat.symbol, "")
            ?: ""
          views.totalBalanceValueAppcc.text = "${
            it.creditsBalance.token.amount.toString().formatMoney()
          } ${it.creditsBalance.token.symbol}"

          views.titleBalanceAppcValue.text = "${
            it.appcBalance.token.amount.toString().formatMoney()
          } ${it.appcBalance.token.symbol}"

          views.titleBalanceAppcCreditsValue.text = "${
            it.creditsBalance.token.amount.toString().formatMoney()
          } ${it.creditsBalance.token.symbol}"

          views.titleBalanceEthereumValue.text = "${
            it.ethBalance.token.amount.toString().formatMoney()
          } ${it.ethBalance.token.symbol}"
        }
      }

      ManageWalletViewModel.UiState.Loading -> {}
      else -> {}
    }

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

  override fun onStateChanged(state: ManageWalletBalanceBottomSheetState) {}

  override fun onSideEffect(sideEffect: ManageWalletBalanceBottomSheetSideEffect) {
    when (sideEffect) {
      is ManageWalletBalanceBottomSheetSideEffect.NavigateBack -> navigator.navigateBack()
    }
  }

}
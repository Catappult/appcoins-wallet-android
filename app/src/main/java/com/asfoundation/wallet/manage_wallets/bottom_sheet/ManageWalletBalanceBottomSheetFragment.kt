package com.asfoundation.wallet.manage_wallets.bottom_sheet


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.utils.android_common.AmountUtils.formatMoney
import com.appcoins.wallet.core.utils.android_common.extensions.getSerializableExtra
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletBalance
import com.asf.wallet.R
import com.asf.wallet.databinding.ManageWalletBalanceBottomSheetLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ManageWalletBalanceBottomSheetFragment : BottomSheetDialogFragment(),
  SingleStateFragment<ManageWalletBalanceBottomSheetState, ManageWalletBalanceBottomSheetSideEffect> {


  @Inject
  lateinit var navigator: ManageWalletBalanceBottomSheetNavigator

  private val views by viewBinding(ManageWalletBalanceBottomSheetLayoutBinding::bind)

  companion object {
    const val WALLET_BALANCE_MODEL = "wallet_balance_model"

    @JvmStatic
    fun newInstance(): ManageWalletBalanceBottomSheetFragment {
      return ManageWalletBalanceBottomSheetFragment()
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ManageWalletBalanceBottomSheetLayoutBinding.inflate(inflater).root
  }

  @SuppressLint("SetTextI18n")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val walletBalanceModel = getSerializableExtra<WalletBalance>(WALLET_BALANCE_MODEL)

    walletBalanceModel?.let {
      views.totalBalanceValue.text = it.creditsOnlyFiat.amount
        .toString()
        .formatMoney(it.creditsOnlyFiat.symbol, "")
        ?: ""
    }
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
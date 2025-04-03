package com.asfoundation.wallet.home.bottom_sheet


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.HomeDetailsBalanceBottomSheetLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeDetailsBalanceBottomSheetFragment : BottomSheetDialogFragment() {
  private val views by viewBinding(HomeDetailsBalanceBottomSheetLayoutBinding::bind)

  companion object {
    const val BALANCE_VALUE = "balance_value"
    const val BALANCE_CURRENCY = "balance_currency"
    @JvmStatic
    fun newInstance(): HomeDetailsBalanceBottomSheetFragment {
      return HomeDetailsBalanceBottomSheetFragment()
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = HomeDetailsBalanceBottomSheetLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val walletBalance = arguments?.getString(BALANCE_VALUE)
    val walletBalanceCurrency = arguments?.getString(BALANCE_CURRENCY)
    if (!walletBalance.isNullOrEmpty() && !walletBalanceCurrency.isNullOrEmpty()) {
      views.valueText.text = walletBalance
      views.currencyText.text = walletBalanceCurrency
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

}
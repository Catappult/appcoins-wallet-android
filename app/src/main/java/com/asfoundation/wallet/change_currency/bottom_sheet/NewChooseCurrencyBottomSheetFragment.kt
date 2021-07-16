package com.asfoundation.wallet.change_currency.bottom_sheet

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.ChooseCurrencyBottomSheetBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.change_currency.FiatCurrency
import com.asfoundation.wallet.di.DaggerBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import javax.inject.Inject

class NewChooseCurrencyBottomSheetFragment : DaggerBottomSheetDialogFragment(),
    ChooseCurrencyBottomSheetView,
    SingleStateFragment<ChooseCurrencyBottomSheetState, ChooseCurrencyBottomSideEffect> {


  @Inject
  lateinit var chooseCurrencyBottomSheetViewModelFactory: ChooseCurrencyBottomSheetViewModelFactory

  @Inject
  lateinit var navigator: ChooseCurrencyBottomSheetNavigator

  private val viewModel: NewChooseCurrencyBottomSheetViewModel by viewModels { chooseCurrencyBottomSheetViewModelFactory }
  private val views by viewBinding(ChooseCurrencyBottomSheetBinding::bind)

  companion object {

    const val FLAG = "flag"
    const val CURRENCY = "currency"
    const val LABEL = "label"
    const val SIGN = "sign"

    @JvmStatic
    fun newInstance(fiatCurrency: FiatCurrency): ChooseCurrencyBottomSheetFragment {
      return ChooseCurrencyBottomSheetFragment()
          .apply {
            arguments = Bundle().apply {
              putString(FLAG, fiatCurrency.flag)
              putString(CURRENCY, fiatCurrency.currency)
              putString(LABEL, fiatCurrency.label)
              putString(SIGN, fiatCurrency.sign)
            }
          }
    }
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.chooseCurrencyConfirmationButton.setOnClickListener { viewModel.currencyConfirmationClick() }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun getTheme(): Int {
    return R.style.AppBottomSheetDialogTheme
  }


  override fun onStateChanged(state: ChooseCurrencyBottomSheetState) {
    setChooseCurrencyBottomSheetData(state.selectedCurrency, state.selectedFlag,
        state.selectedLabel)
    setSelectedConfirmation(state.selectedConfirmationAsync)
  }

  override fun onSideEffect(sideEffect: ChooseCurrencyBottomSideEffect) {
    when (sideEffect) {
      is ChooseCurrencyBottomSideEffect.ShowConfirmationLoading -> showLoading()
      is ChooseCurrencyBottomSideEffect.NavigateBack -> navigator.navigateBack()
    }
  }

  fun setChooseCurrencyBottomSheetData(selectedCurrency: String, selectedFlag: String,
                                       selectedLabel: String) {
    setCurrencyFlag(selectedFlag)
    setCurrencyShort(selectedCurrency)
    setCurrencyLabel(selectedLabel)
  }

  override fun setCurrencyFlag(currencyFlag: String) {
//    GlideToVectorYou
//        .init()
//        .with(context)
//        .load(Uri.parse(currencyFlag), views.chooseCurrencyFlag)
    //TODO
  }

  override fun setCurrencyShort(currencyShort: String) {
    Log.d("APPC-2472", "setCurrencyShort: short: $currencyShort")
    views.chooseCurrencyShort.text = currencyShort
  }

  override fun setCurrencyLabel(currencyLabel: String) {
    Log.d("APPC-2472", "setCurrencyLabel: label: $currencyLabel")
    views.chooseCurrencyLabel.text = currencyLabel
  }

  fun setSelectedConfirmation(selectedConfirmationAsync: Async<Unit>) {
    when (selectedConfirmationAsync) {
      Async.Uninitialized,
      is Async.Loading -> {
        if (selectedConfirmationAsync.value == null) {
          showLoading()
        }
      }
      is Async.Fail -> {
      }
      is Async.Success -> {
        navigator.navigateBack()
      }
    }
  }

  override fun showLoading() {
    views.chooseCurrencyFlag.visibility = View.GONE
    views.chooseCurrencyShort.visibility = View.GONE
    views.chooseCurrencyLabel.visibility = View.GONE
    views.chooseCurrencyConfirmationButton.visibility = View.GONE

    views.chooseCurrencySystemView.showOnlyProgress()
    views.chooseCurrencySystemView.visibility = View.VISIBLE
  }
}
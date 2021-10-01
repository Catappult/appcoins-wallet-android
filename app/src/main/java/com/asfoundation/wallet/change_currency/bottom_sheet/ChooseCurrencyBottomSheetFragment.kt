package com.asfoundation.wallet.change_currency.bottom_sheet

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.ChooseCurrencyBottomSheetBinding
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.change_currency.FiatCurrency
import com.asfoundation.wallet.di.DaggerBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import javax.inject.Inject

class ChooseCurrencyBottomSheetFragment : DaggerBottomSheetDialogFragment(),
    ChooseCurrencyBottomSheetView,
    SingleStateFragment<ChooseCurrencyBottomSheetState, ChooseCurrencyBottomSideEffect> {


  @Inject
  lateinit var chooseCurrencyBottomSheetViewModelFactory: ChooseCurrencyBottomSheetViewModelFactory

  @Inject
  lateinit var navigator: ChooseCurrencyBottomSheetNavigator

  private val viewModel: ChooseCurrencyBottomSheetViewModel by viewModels { chooseCurrencyBottomSheetViewModelFactory }
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

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.choose_currency_bottom_sheet, container, false)
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
    Log.d("APPC-2472", "ChooseCurrencyBottomSheetFragment: onStateChanged: $state")
    setChooseCurrencyBottomSheetData(state.selectedCurrency, state.selectedFlag,
        state.selectedLabel)
    setSelectedConfirmation(state.selectedConfirmationAsync)
  }

  override fun onSideEffect(sideEffect: ChooseCurrencyBottomSideEffect) {
    when (sideEffect) {
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
    GlideApp
        .with(requireContext())
        .load(Uri.parse(currencyFlag))
        .circleCrop()
        .into(views.chooseCurrencyFlag)
  }

  override fun setCurrencyShort(currencyShort: String) {
    views.chooseCurrencyShort.text = currencyShort
  }

  override fun setCurrencyLabel(currencyLabel: String) {
    views.chooseCurrencyLabel.text = currencyLabel
  }

  fun setSelectedConfirmation(selectedConfirmationAsync: Async<Unit>) {
    when (selectedConfirmationAsync) {
      is Async.Uninitialized -> {
      }
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
    Log.d("APPC-2472", "ChooseCurrencyBottomSheetFragment: showLoading")
    views.chooseCurrencyFlag.visibility = View.GONE
    views.chooseCurrencyShort.visibility = View.GONE
    views.chooseCurrencyLabel.visibility = View.GONE
    views.chooseCurrencyConfirmationButton.visibility = View.GONE

    views.chooseCurrencySystemView.visibility = View.VISIBLE
    views.chooseCurrencySystemView.showProgress(true)
  }
}
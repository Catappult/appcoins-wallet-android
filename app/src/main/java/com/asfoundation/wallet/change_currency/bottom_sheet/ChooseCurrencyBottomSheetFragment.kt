package com.asfoundation.wallet.change_currency.bottom_sheet

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.ChooseCurrencyBottomSheetBinding
import com.asfoundation.wallet.GlideApp
import com.appcoins.wallet.ui.arch.Async
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.asfoundation.wallet.change_currency.FiatCurrencyEntity
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChooseCurrencyBottomSheetFragment : BottomSheetDialogFragment(),
  com.appcoins.wallet.ui.arch.SingleStateFragment<ChooseCurrencyBottomSheetState, ChooseCurrencyBottomSideEffect> {


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
    fun newInstance(fiatCurrency: FiatCurrencyEntity): ChooseCurrencyBottomSheetFragment {
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
    return R.style.AppBottomSheetDialogThemeDraggable
  }


  override fun onStateChanged(state: ChooseCurrencyBottomSheetState) {
    setChooseCurrencyBottomSheetData(state.selectedCurrency, state.selectedFlag,
        state.selectedLabel)
    setSelectedConfirmation(state.selectedConfirmationAsync)
  }

  override fun onSideEffect(sideEffect: ChooseCurrencyBottomSideEffect) {
    when (sideEffect) {
      is ChooseCurrencyBottomSideEffect.NavigateBack -> navigator.navigateBack()
    }
  }

  fun setChooseCurrencyBottomSheetData(selectedCurrency: String, selectedFlag: String?,
                                       selectedLabel: String) {
    setCurrencyFlag(selectedFlag)
    setCurrencyShort(selectedCurrency)
    setCurrencyLabel(selectedLabel)
  }

  fun setCurrencyFlag(currencyFlag: String?) {
    GlideApp
      .with(requireContext())
      .load(
        if (currencyFlag != null)
          Uri.parse(currencyFlag)
        else
          R.drawable.currency_flag_placeholder
      )
      .transition(DrawableTransitionOptions.withCrossFade())
      .circleCrop()
      .into(views.chooseCurrencyFlag)
  }

  fun setCurrencyShort(currencyShort: String) {
    views.chooseCurrencyShort.text = currencyShort
  }

  fun setCurrencyLabel(currencyLabel: String) {
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

  fun showLoading() {
    views.chooseCurrencyFlag.visibility = View.GONE
    views.chooseCurrencyShort.visibility = View.GONE
    views.chooseCurrencyLabel.visibility = View.GONE
    views.chooseCurrencyConfirmationButton.visibility = View.GONE

    views.chooseCurrencySystemView.visibility = View.VISIBLE
    views.chooseCurrencySystemView.showProgress(true)
  }
}
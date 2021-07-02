package com.asfoundation.wallet.ui.settings.change_currency.bottom_sheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.asf.wallet.R
import com.asfoundation.wallet.di.DaggerBottomSheetDialogFragment
import com.asfoundation.wallet.ui.settings.change_currency.FiatCurrency
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.choose_currency_bottom_sheet.*
import javax.inject.Inject

class ChooseCurrencyBottomSheetFragment : DaggerBottomSheetDialogFragment(),
    ChooseCurrencyBottomSheetView {

  @Inject
  lateinit var chooseCurrencyBottomSheetViewModelFactory: ChooseCurrencyBottomSheetViewModelFactory

  lateinit var viewModel: ChooseCurrencyBottomSheetViewModel

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
    viewModel =
        ViewModelProviders.of(this,
            chooseCurrencyBottomSheetViewModelFactory)[ChooseCurrencyBottomSheetViewModel::class.java]
//    dialog?.setCanceledOnTouchOutside(false)
  }

  override fun getTheme(): Int {
    return R.style.AppBottomSheetDialogTheme
  }

  override fun setCurrencyFlag(currencyFlag: String) {
//    GlideToVectorYou
//        .init()
//        .with(context)
//        .load(Uri.parse(currencyFlag), choose_currency_flag)
    //TODO
  }

  override fun setCurrencyShort(currencyShort: String) {
    Log.d("APPC-2472", "setCurrencyShort: short: $currencyShort")
    choose_currency_short.text = currencyShort
  }

  override fun setCurrencyLabel(currencyLabel: String) {
    Log.d("APPC-2472", "setCurrencyLabel: label: $currencyLabel")
    choose_currency_label.text = currencyLabel
  }

  override fun getConfirmationClick(): Observable<Any> {
//    val behavior = BottomSheetBehavior.from(requireView().parent as View)
//    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    return RxView.clicks(choose_currency_confirmation_button)
  }

  override fun onDestroyView() {
    super.onDestroyView()
  }
}
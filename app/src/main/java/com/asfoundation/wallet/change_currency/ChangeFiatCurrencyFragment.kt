package com.asfoundation.wallet.change_currency

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentChangeFiatCurrencyBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.change_currency.bottom_sheet.ChooseCurrencyBottomSheetFragment
import com.asfoundation.wallet.change_currency.list.ChangeFiatCurrencyController
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import javax.inject.Inject

class ChangeFiatCurrencyFragment : BasePageViewFragment(),
    SingleStateFragment<ChangeFiatCurrencyState, ChangeFiatCurrencySideEffect> {

  @Inject
  lateinit var changeFiatCurrencyViewModelFactory: ChangeFiatCurrencyViewModelFactory

  private val viewModel: ChangeFiatCurrencyViewModel by viewModels { changeFiatCurrencyViewModelFactory }
  private val views by viewBinding(FragmentChangeFiatCurrencyBinding::bind)

  private val changeFiatCurrencyController = ChangeFiatCurrencyController()

  override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?,
                            @Nullable savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_change_fiat_currency, container, false)
  }


  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    changeFiatCurrencyController.clickListener = { fiatCurrency ->
      ChooseCurrencyBottomSheetFragment.newInstance(fiatCurrency)
          .show(this.requireFragmentManager(), "ChooseCurrencyBottomSheet")
    }
    views.fragmentChangeFiatCurrencyList.setController(changeFiatCurrencyController)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onStateChanged(state: ChangeFiatCurrencyState) {
    setChangeFiatCurrencyModel(state.changeFiatCurrencyAsync)
  }

  override fun onSideEffect(sideEffect: ChangeFiatCurrencySideEffect) {
    TODO("Not yet implemented")
  }

  fun setChangeFiatCurrencyModel(asyncChangeFiatCurrency: Async<ChangeFiatCurrency>) {
    when (asyncChangeFiatCurrency) {
      Async.Uninitialized,
      is Async.Loading -> {
        if (asyncChangeFiatCurrency.value == null) {
          showLoading()
        }
      }
      is Async.Fail -> {
      }
      is Async.Success -> {
        setChangeFiatCurrency(asyncChangeFiatCurrency())
      }
    }
  }

  private fun showLoading() {
    views.fragmentChangeFiatCurrencyList.visibility = View.INVISIBLE
    views.changeFiatSystemView.showOnlyProgress()
    views.changeFiatSystemView.visibility = View.VISIBLE

    changeFiatCurrencyController.clickListener = { fiatCurrency ->
      ChooseCurrencyBottomSheetFragment.newInstance(fiatCurrency)
          .show(this.requireFragmentManager(), "ChooseCurrencyBottomSheet")
    }
    views.fragmentChangeFiatCurrencyList.setController(changeFiatCurrencyController)
  }

  fun setChangeFiatCurrency(asyncChangeFiatCurrency: ChangeFiatCurrency) {
    views.fragmentChangeFiatCurrencyList.visibility = View.VISIBLE
    views.changeFiatSystemView.visibility = View.GONE
    changeFiatCurrencyController.setData(asyncChangeFiatCurrency)
  }

  companion object {
    fun newInstance() = ChangeFiatCurrencyFragment()
  }
}
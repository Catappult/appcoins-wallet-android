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
import com.appcoins.wallet.ui.arch.Async
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.asfoundation.wallet.change_currency.list.ChangeFiatCurrencyController
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangeFiatCurrencyFragment : BasePageViewFragment(),
  com.appcoins.wallet.ui.arch.SingleStateFragment<ChangeFiatCurrencyState, ChangeFiatCurrencySideEffect> {

  @Inject
  lateinit var changeFiatCurrencyNavigator: ChangeFiatCurrencyNavigator

  private val viewModel: ChangeFiatCurrencyViewModel by viewModels()
  private val views by viewBinding(FragmentChangeFiatCurrencyBinding::bind)

  private val changeFiatCurrencyController = ChangeFiatCurrencyController()

  override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?,
                            @Nullable savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_change_fiat_currency, container, false)
  }


  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    changeFiatCurrencyController.clickListener = { fiatCurrency ->
      changeFiatCurrencyNavigator.openBottomSheet(fiatCurrency)
    }
    views.fragmentChangeFiatCurrencyList.setController(changeFiatCurrencyController)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onStateChanged(state: ChangeFiatCurrencyState) {
    setChangeFiatCurrencyModel(state.changeFiatCurrencyAsync)
  }

  override fun onSideEffect(sideEffect: ChangeFiatCurrencySideEffect) = Unit

  fun setChangeFiatCurrencyModel(asyncChangeFiatCurrency: com.appcoins.wallet.ui.arch.Async<ChangeFiatCurrency>) {
    when (asyncChangeFiatCurrency) {
      com.appcoins.wallet.ui.arch.Async.Uninitialized,
      is com.appcoins.wallet.ui.arch.Async.Loading -> {
        if (asyncChangeFiatCurrency.value == null) {
          showLoading()
        }
      }
      is com.appcoins.wallet.ui.arch.Async.Fail -> {
      }
      is com.appcoins.wallet.ui.arch.Async.Success -> {
        setChangeFiatCurrency(asyncChangeFiatCurrency())
      }
    }
  }

  private fun showLoading() {
    views.fragmentChangeFiatCurrencyList.visibility = View.INVISIBLE
    views.changeFiatSystemView.showProgress(true)
    views.changeFiatSystemView.visibility = View.VISIBLE

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
package com.asfoundation.wallet.ui.settings.change_currency

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.lifecycle.ViewModelProviders
import com.asf.wallet.R
import com.asfoundation.wallet.ui.settings.change_currency.list.ChangeFiatCurrencyController
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_change_fiat_currency.*
import javax.inject.Inject

class ChangeFiatCurrencyFragment : DaggerFragment() {


  @Inject
  lateinit var changeFiatCurrencyViewModelFactory: ChangeFiatCurrencyViewModelFactory

  lateinit var viewModel: ChangeFiatCurrencyViewModel

  private val changeFiatCurrencyController = ChangeFiatCurrencyController()

  companion object {
    fun newInstance() = ChangeFiatCurrencyFragment()
  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    changeFiatCurrencyController.clickListener = { fiatCurrency ->
      navigator.showBottom()
    }
    fragment_change_fiat_currency_list.setController(changeFiatCurrencyController)
  }

  @Nullable
  override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?,
                            @Nullable savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_change_fiat_currency, container, false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    viewModel =
        ViewModelProviders.of(this,
            changeFiatCurrencyViewModelFactory)[ChangeFiatCurrencyViewModel::class.java]
    viewModel.changeFiatCurrencyLiveData
        .observe(this, {
          this.showCurrencies(it)
        })
  }

  fun showCurrencies(currency: ChangeFiatCurrency) {
    changeFiatCurrencyController.setData(currency)
  }
}
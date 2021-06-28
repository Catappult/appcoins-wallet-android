package com.asfoundation.wallet.ui.settings.change_currency

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.settings.change_currency.adapter.ChangeFiatCurrencyAdapter
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_change_fiat_currency.*

import org.jetbrains.annotations.NotNull
import javax.inject.Inject

class ChangeFiatCurrencyFragment : DaggerFragment() {

  lateinit var currencyAdapter: ChangeFiatCurrencyAdapter

  @Inject
  lateinit var changeFiatCurrencyViewModelFactory: ChangeFiatCurrencyViewModelFactory

  lateinit var viewModel: ChangeFiatCurrencyViewModel

  companion object {
    fun newInstance() = ChangeFiatCurrencyFragment()
  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    currencyAdapter = ChangeFiatCurrencyAdapter(requireFragmentManager())
    fragment_change_fiat_currency_list.layoutManager = LinearLayoutManager(context);
    fragment_change_fiat_currency_list.adapter = currencyAdapter
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
    viewModel.currencyList()
        .observe(this, {
          this.showCurrencies(it)
        })
  }

  fun showCurrencies(@NotNull currencyList: List<FiatCurrency>) {
    currencyAdapter.setCurrencies(currencyList)
    fragment_change_fiat_currency_list.scheduleLayoutAnimation()
    fragment_change_fiat_currency_list.visibility = View.VISIBLE
  }

  fun refreshCurrencies(@NotNull currencyList: List<FiatCurrency>) {
    currencyAdapter.setCurrencies(currencyList)
    fragment_change_fiat_swipe_refresh.isRefreshing = false
    fragment_change_fiat_currency_list.scheduleLayoutAnimation()
  }
}
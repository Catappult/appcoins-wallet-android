package com.asfoundation.wallet.ui.settings.change_currency.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.ui.settings.change_currency.FiatCurrency
import com.asfoundation.wallet.ui.settings.change_currency.FiatCurrencyClickListener
import com.asfoundation.wallet.ui.settings.change_currency.bottom_sheet.ChooseCurrencyBottomSheetFragment
import java.util.*


class ChangeFiatCurrencyAdapter(val fragmentManager: FragmentManager) :
    RecyclerView.Adapter<ChangeFiatCurrencyItemViewHolder>() {
  lateinit var selectedCurrency: FiatCurrency
  private val currencyList: MutableList<FiatCurrency> = ArrayList()
  private var fiatCurrencyClickListener: FiatCurrencyClickListener

  init {
    fiatCurrencyClickListener = object : FiatCurrencyClickListener {
      override fun onClick(view: View?, position: Int) {
        handleClick(position)
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup,
                                  viewType: Int): ChangeFiatCurrencyItemViewHolder {
    return ChangeFiatCurrencyItemViewHolder(LayoutInflater.from(parent.context)
        .inflate(R.layout.item_change_fiat_currency, parent, false), fiatCurrencyClickListener)
  }

  override fun onBindViewHolder(holder: ChangeFiatCurrencyItemViewHolder, position: Int) {
    holder.setCurrency(currencyList[position], currencyList.contains(position))
  }

  override fun getItemCount(): Int {
    return currencyList.size
  }

  fun setCurrencies(list: List<FiatCurrency>) {
    if (list != currencyList) {
      currencyList.clear()
      currencyList.addAll(list)
      notifyDataSetChanged()
    }
  }

  fun handleClick(position: Int) {
    val bottomSheet = ChooseCurrencyBottomSheetFragment.newInstance(currencyList[position].flag!!,
        currencyList[position].currency!!, currencyList[position].label!!)
    bottomSheet.show(fragmentManager, "ChooseCurrencyBottomSheet")

    notifyItemChanged(position)
  }

  fun setSelected(position: Int) {
    notifyItemChanged(position)
  }
}
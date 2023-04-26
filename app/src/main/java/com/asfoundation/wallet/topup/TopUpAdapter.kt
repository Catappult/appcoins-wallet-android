package com.asfoundation.wallet.topup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.core.utils.jvm_common.NumberFormatterUtils
import com.asf.wallet.databinding.ItemTopValueBinding
import rx.functions.Action1


class TopUpAdapter(private val listener: Action1<com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue>
) : ListAdapter<com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue, TopUpAdapter.TopUpViewHolder>(FiatValueCallback()) {


  class FiatValueCallback : DiffUtil.ItemCallback<com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue>() {
    override fun areItemsTheSame(oldItem: com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue, newItem: com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue): Boolean {
      return oldItem.amount == newItem.amount
    }

    override fun areContentsTheSame(oldItem: com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue, newItem: com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue): Boolean {
      return oldItem == newItem
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopUpViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return TopUpViewHolder(inflater.inflate(R.layout.item_top_value, parent, false))
  }

  override fun onBindViewHolder(holder: TopUpViewHolder, position: Int) {
    holder.bind(getItem(position), listener)
  }

  class TopUpViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val binding by lazy { ItemTopValueBinding.bind(itemView) }

    fun bind(fiatValue: com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue, listener: Action1<com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue>) {
      val formatter = NumberFormatterUtils.create()
      val text = fiatValue.symbol + formatter.formatNumberWithSuffix(fiatValue.amount.toFloat())

      binding.value.text = text
      itemView.setOnClickListener { listener.call(fiatValue) }
    }
  }

}
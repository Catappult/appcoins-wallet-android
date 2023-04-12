package com.asfoundation.wallet.topup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.FiatValue
import com.appcoins.wallet.core.utils.jvm_common.NumberFormatterUtils
import com.asf.wallet.databinding.ItemTopValueBinding
import rx.functions.Action1


class TopUpAdapter(private val listener: Action1<FiatValue>
) : ListAdapter<FiatValue, TopUpAdapter.TopUpViewHolder>(FiatValueCallback()) {


  class FiatValueCallback : DiffUtil.ItemCallback<FiatValue>() {
    override fun areItemsTheSame(oldItem: FiatValue, newItem: FiatValue): Boolean {
      return oldItem.amount == newItem.amount
    }

    override fun areContentsTheSame(oldItem: FiatValue, newItem: FiatValue): Boolean {
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

    fun bind(fiatValue: FiatValue, listener: Action1<FiatValue>) {
      val formatter = NumberFormatterUtils.create()
      val text = fiatValue.symbol + formatter.formatNumberWithSuffix(fiatValue.amount.toFloat())

      binding.value.text = text
      itemView.setOnClickListener { listener.call(fiatValue) }
    }
  }

}
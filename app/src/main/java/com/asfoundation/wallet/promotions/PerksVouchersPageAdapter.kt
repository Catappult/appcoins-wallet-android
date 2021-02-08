package com.asfoundation.wallet.promotions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import io.reactivex.subjects.PublishSubject

class PerksVouchersPageAdapter(private var items: List<List<Promotion>>,
                               private var clickSubject: PublishSubject<PromotionClick>) :
    RecyclerView.Adapter<PerksVouchersViewHolder>() {

  override fun getItemCount() = items.size

  override fun onCreateViewHolder(container: ViewGroup, viewType: Int): PerksVouchersViewHolder {
    val view = LayoutInflater.from(container.context)
        .inflate(R.layout.perk_vouchers_page, container, false)
    return PerksVouchersViewHolder(view, clickSubject)
  }

  override fun onBindViewHolder(holder: PerksVouchersViewHolder, position: Int) {
    holder.bind(items[position], position)
  }

  fun setItems(vouchersPerkList: List<List<Promotion>>) {
    items = vouchersPerkList
    notifyDataSetChanged()
  }
}
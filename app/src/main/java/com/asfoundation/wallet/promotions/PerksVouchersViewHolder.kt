package com.asfoundation.wallet.promotions

import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.promotions.model.PerkPromotion
import com.asfoundation.wallet.promotions.model.Promotion
import com.asfoundation.wallet.promotions.model.PromotionClick
import com.asfoundation.wallet.promotions.model.VoucherItem
import com.asfoundation.wallet.promotions.ui.list.PromotionsAdapter
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_page_perk_vouchers.view.*

class PerksVouchersViewHolder(itemView: View,
                              private val clickSubject: PublishSubject<PromotionClick>) :
    RecyclerView.ViewHolder(itemView) {

  companion object {
    const val VOUCHER_POSITION = 0
    const val PERKS_POSITION = 1
  }

  fun bind(list: List<Promotion>, position: Int) {
    var adapter: PromotionsAdapter? = null
    if (position == VOUCHER_POSITION) {
      val voucherList = list.filterIsInstance<VoucherItem>()
      if (voucherList.isEmpty()) setVouchersEmptyState()
      else {
        itemView.vouchers_empty_screen.visibility = View.INVISIBLE
        adapter = VouchersAdapter(voucherList, clickSubject)
      }
    } else {
      val perksList = list.filterIsInstance<PerkPromotion>()
      if (perksList.isEmpty()) setPerksEmptyState()
      else {
        itemView.vouchers_empty_screen.visibility = View.INVISIBLE
        adapter = PerksAdapter(perksList, clickSubject)
      }
    }
    if (adapter != null) {
      itemView.page_recycler.visibility = View.VISIBLE
      itemView.page_recycler.adapter = adapter
    }
  }

  private fun setPerksEmptyState() {
    itemView.empty_screen_image.setImageDrawable(
        ResourcesCompat.getDrawable(itemView.resources, R.drawable.perks_empty_state_image,
            null))
    itemView.empty_screen_text.text =
        itemView.context.getString(R.string.promotions_empty_promotions_body)
    itemView.vouchers_empty_screen.visibility = View.VISIBLE
  }

  private fun setVouchersEmptyState() {
    itemView.empty_screen_image.setImageDrawable(
        ResourcesCompat.getDrawable(itemView.resources, R.drawable.vouchers_empty_state_image,
            null))
    itemView.empty_screen_text.text = itemView.context.getString(R.string.voucher_empty)
    itemView.vouchers_empty_screen.visibility = View.VISIBLE
  }
}

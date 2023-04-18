package com.asfoundation.wallet.home.ui.list.transactions

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.home.ui.list.HomeListClick
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.transactions.TransactionDetails
import com.appcoins.wallet.ui.widgets.BaseViewHolder

@EpoxyModelClass
abstract class PerkModel : EpoxyModelWithHolder<PerkModel.PerkHolder>() {

  @EpoxyAttribute
  var transaction: Transaction? = null

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var clickListener: ((HomeListClick) -> Unit)? = null

  override fun getDefaultLayout(): Int = R.layout.item_transaction_perk_bonus

  override fun bind(holder: PerkHolder) {
    transaction?.let { tx ->
      handleIcon(holder, tx.details)

      if (!tx.title.isNullOrEmpty()) {
        holder.bonusTitle.text = tx.title
      } else {
        holder.bonusTitle.visibility = View.GONE
      }

      holder.bonusDescription.text = tx.description
      holder.itemView.setOnClickListener {
        clickListener?.invoke(HomeListClick.TransactionClick(tx))
      }
    }
  }

  private fun handleIcon(holder: PerkHolder, details: TransactionDetails?) {
    var uri: String? = null
    val icon: TransactionDetails.Icon?
    if (details != null) {
      icon = details.icon
      when (icon?.type) {
        TransactionDetails.Icon.Type.FILE -> uri = "file:" + icon.uri
        TransactionDetails.Icon.Type.URL -> uri = icon.uri
        else -> {}
      }
    }
    GlideApp.with(holder.itemView.context)
        .load(uri)
        .error(R.drawable.transactions_promotion_bonus)
        .into(holder.image)
  }

  class PerkHolder : BaseViewHolder() {
    val image by bind<ImageView>(R.id.img)
    val bonusTitle by bind<TextView>(R.id.bonus_title)
    val bonusDescription by bind<TextView>(R.id.bonus_description)
  }
}
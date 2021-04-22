package com.asfoundation.wallet.ui.transactions.models

import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.C
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.transactions.TransactionDetails
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import java.math.BigDecimal
import java.util.*
import kotlin.math.pow

@EpoxyModelClass
abstract class TransactionModel : EpoxyModelWithHolder<TransactionModel.TransactionHolder>() {

  @EpoxyAttribute
  var transaction: Transaction? = null

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var defaultAddress: String? = null

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var currency: String? = null

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var formatter: CurrencyFormatUtils? = null

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var clickListener: ((Transaction) -> Unit)? = null

  override fun getDefaultLayout(): Int = R.layout.item_transaction

  override fun bind(holder: TransactionHolder) {
    transaction?.let { tx ->
      val actualCurrency = if (!TextUtils.isEmpty(tx.currency)) {
        tx.currency
      } else {
        currency
      }
      initializeView(holder, tx.from, tx.to, actualCurrency, tx.value, tx.status, tx.details,
          tx.type, tx.linkedTx)
      holder.itemView.setOnClickListener { clickListener?.invoke(tx) }
    }
  }

  private fun initializeView(holder: TransactionHolder, from: String, to: String,
                             currency: String?, value: String,
                             status: Transaction.TransactionStatus, details: TransactionDetails?,
                             type: Transaction.TransactionType, linkedTx: List<Transaction>?) {
    val isSent = from.equals(defaultAddress, ignoreCase = true)
    holder.revertMessage.visibility = View.GONE

    var icon: TransactionDetails.Icon? = null
    var uri: String? = null
    details?.let {
      icon = details.icon
      uri = when (icon?.type) {
        TransactionDetails.Icon.Type.FILE -> "file:" + icon?.uri
        TransactionDetails.Icon.Type.URL -> icon?.uri
        null -> null
      }
    }

    var currencySymbol = currency
    val transactionTypeIcon: Int
    when (type) {
      Transaction.TransactionType.IAP, Transaction.TransactionType.IAP_OFFCHAIN -> {
        transactionTypeIcon = R.drawable.ic_transaction_iab
        setTypeIconVisibilityBasedOnDescription(holder, details, uri)
      }
      Transaction.TransactionType.BONUS_REVERT, Transaction.TransactionType.TOP_UP_REVERT -> {
        transactionTypeIcon = R.drawable.ic_transaction_revert
        holder.typeIcon.visibility = View.VISIBLE
        setRevertMessage(holder, linkedTx)
        currencySymbol = WalletCurrency.CREDITS.symbol
      }
      Transaction.TransactionType.IAP_REVERT -> {
        transactionTypeIcon = R.drawable.ic_transaction_revert
        holder.typeIcon.visibility = View.VISIBLE
        setRevertMessage(holder, linkedTx)
      }
      Transaction.TransactionType.ADS, Transaction.TransactionType.ADS_OFFCHAIN -> {
        transactionTypeIcon = R.drawable.ic_transaction_poa
        setTypeIconVisibilityBasedOnDescription(holder, details, uri)
        currencySymbol = WalletCurrency.CREDITS.symbol
      }
      Transaction.TransactionType.BONUS -> {
        holder.typeIcon.visibility = View.GONE
        transactionTypeIcon = R.drawable.ic_transaction_peer
        currencySymbol = WalletCurrency.CREDITS.symbol
      }
      Transaction.TransactionType.TOP_UP -> {
        holder.typeIcon.visibility = View.GONE
        transactionTypeIcon = R.drawable.transaction_type_top_up
        currencySymbol = WalletCurrency.CREDITS.symbol
      }
      Transaction.TransactionType.TRANSFER_OFF_CHAIN -> {
        holder.typeIcon.visibility = View.GONE
        transactionTypeIcon = R.drawable.transaction_type_transfer_off_chain
        currencySymbol = WalletCurrency.CREDITS.symbol
      }
      else -> {
        transactionTypeIcon = R.drawable.ic_transaction_peer
        setTypeIconVisibilityBasedOnDescription(holder, details, uri)
      }
    }

    if (details != null) {
      when (transaction!!.type) {
        Transaction.TransactionType.BONUS -> holder.address.setText(R.string.transaction_type_bonus)
        Transaction.TransactionType.TOP_UP -> holder.address.setText(R.string.topup_home_button)
        Transaction.TransactionType.TRANSFER_OFF_CHAIN -> holder.address.setText(
            R.string.transaction_type_p2p)
        Transaction.TransactionType.TOP_UP_REVERT -> holder.address.setText(
            R.string.transaction_type_reverted_topup_title)
        Transaction.TransactionType.BONUS_REVERT -> holder.address.setText(
            R.string.transaction_type_reverted_bonus_title)
        Transaction.TransactionType.IAP_REVERT -> holder.address.setText(
            R.string.transaction_type_reverted_purchase_title)
        else -> {
          holder.address.text = if (details.sourceName == null) {
            if (isSent) to else from
          } else {
            getSourceText(holder, type, details)
          }
        }
      }
      holder.description.text = if (details.description == null) "" else details.description
    } else {
      holder.address.text = if (isSent) to else from
      holder.description.text = ""
    }

    GlideApp.with(holder.itemView.context)
        .load(uri)
        .apply(RequestOptions.bitmapTransform(CircleCrop())
            .placeholder(transactionTypeIcon)
            .error(transactionTypeIcon))
        .listener(object : RequestListener<Drawable?> {
          override fun onLoadFailed(e: GlideException?, model: Any,
                                    target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
            holder.typeIcon.visibility = View.GONE
            return false
          }

          override fun onResourceReady(resource: Drawable?, model: Any, target: Target<Drawable?>,
                                       dataSource: DataSource, isFirstResource: Boolean): Boolean {
            (holder.typeIcon.findViewById(R.id.icon) as ImageView).setImageResource(
                transactionTypeIcon)
            return false
          }
        })
        .into(holder.srcImage)

    var statusText = R.string.transaction_status_success
    var statusColor = R.color.green

    when (status) {
      Transaction.TransactionStatus.PENDING -> {
        statusText = R.string.transaction_status_pending
        statusColor = R.color.orange
      }
      Transaction.TransactionStatus.FAILED -> {
        statusText = R.string.transaction_status_failed
        statusColor = R.color.red
      }
      else -> Unit
    }

    holder.status.setText(statusText)
    holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context, statusColor))

    var valueStr = value
    if (valueStr == "0") {
      valueStr = "0 "
    } else if (transaction!!.type == Transaction.TransactionType.IAP_REVERT) {
      valueStr = getScaledValue(valueStr, C.ETHER_DECIMALS.toLong(), currencySymbol!!)
    } else {
      valueStr = (if (isSent) "-" else "+") + getScaledValue(valueStr, C.ETHER_DECIMALS.toLong(),
          currencySymbol!!)
    }

    holder.currency.text = currencySymbol
    holder.value.text = valueStr
  }

  private fun getScaledValue(valueStr: String, decimals: Long, currencySymbol: String): String {
    val walletCurrency = WalletCurrency.mapToWalletCurrency(currencySymbol);
    val value = BigDecimal(valueStr).divide(BigDecimal(10.toDouble()
        .pow(decimals.toDouble())));
    return formatter!!.formatCurrency(value, walletCurrency)
  }

  private fun setRevertMessage(holder: TransactionHolder, linkedTx: List<Transaction>?) {
    var message: String? = null
    if (linkedTx == null || linkedTx.isEmpty()) {
      holder.revertMessage.visibility = View.GONE
    } else {
      val linkedTx = linkedTx[0]
      if (transaction!!.type == Transaction.TransactionType.BONUS_REVERT) {
        message = holder.itemView.context.getString(R.string.transaction_type_reverted_bonus_body,
            getDate(linkedTx.timeStamp))
      } else if (transaction!!.type == Transaction.TransactionType.IAP_REVERT) {
        message = holder.itemView.context.getString(
            R.string.transaction_type_reverted_purchase_body,
            getDate(linkedTx.timeStamp))
      } else if (transaction!!.type == Transaction.TransactionType.TOP_UP_REVERT) {
        message = holder.itemView.context.getString(R.string.transaction_type_reverted_topup_body,
            getDate(linkedTx.timeStamp))
      }
      holder.revertMessage.text = message
      holder.revertMessage.visibility = View.VISIBLE
    }
  }

  private fun getDate(timeStampInSec: Long): String {
    val cal = Calendar.getInstance(Locale.getDefault())
    cal.timeInMillis = timeStampInSec
    return DateFormat.format("MMM, dd yyyy", cal.time)
        .toString()
  }

  private fun setTypeIconVisibilityBasedOnDescription(holder: TransactionHolder,
                                                      details: TransactionDetails?,
                                                      uri: String?) {
    if (uri == null || details?.sourceName == null) {
      holder.typeIcon.visibility = View.GONE
    } else {
      holder.typeIcon.visibility = View.VISIBLE
    }
  }

  private fun getSourceText(holder: TransactionHolder, type: Transaction.TransactionType,
                            details: TransactionDetails?): String? {
    return if (type == Transaction.TransactionType.BONUS) {
      holder.itemView.context.getString(R.string.gamification_transaction_title,
          details?.sourceName)
    } else {
      details?.sourceName
    }
  }

  class TransactionHolder : BaseViewHolder() {
    val srcImage by bind<ImageView>(R.id.img)
    val typeIcon by bind<View>(R.id.type_icon)
    val address by bind<TextView>(R.id.address)
    val description by bind<TextView>(R.id.description)
    val value by bind<TextView>(R.id.value)
    val currency by bind<TextView>(R.id.currency)
    val status by bind<TextView>(R.id.status)
    val revertMessage by bind<TextView>(R.id.revert_message)
  }
}
package com.asfoundation.wallet.home.ui.list.transactions

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.C
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.home.ui.list.HomeListClick
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.transactions.TransactionDetails
import com.appcoins.wallet.ui.widgets.BaseViewHolder
import com.appcoins.wallet.core.utils.common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.common.WalletCurrency
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import java.math.BigDecimal
import java.util.*
import kotlin.math.pow

@EpoxyModelClass
abstract class TransactionModel : EpoxyModelWithHolder<TransactionModel.TransactionHolder>() {

  @EpoxyAttribute
  lateinit var tx: Transaction

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var defaultAddress: String? = null

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var currency: String? = null

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var formatter: CurrencyFormatUtils? = null

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var clickListener: ((HomeListClick) -> Unit)? = null

  override fun getDefaultLayout(): Int = R.layout.item_transaction

  override fun bind(holder: TransactionHolder) {
    holder.itemView.setOnClickListener { clickListener?.invoke(HomeListClick.TransactionClick(tx)) }

    val context = holder.itemView.context
    val isSent = tx.from.equals(defaultAddress, ignoreCase = true)
    val uri: String? = tx.details?.icon?.let { icon ->
      when (icon.type) {
        TransactionDetails.Icon.Type.FILE -> "file:" + icon.uri
        TransactionDetails.Icon.Type.URL -> icon.uri
        null -> null
      }
    }
    holder.revertMessage.visibility = View.GONE

    // Default values
    var currencySymbol = if (!TextUtils.isEmpty(tx.currency)) tx.currency else currency
    var address = getDefaultAddress(context, isSent)
    var txTypeIcon = R.drawable.ic_transaction_peer
    var isTypeIconVisible = isTypeIconVisibleBasedOnDescription(tx.details, uri)
    var description = tx.details?.description ?: ""

    when (tx.type) {
      Transaction.TransactionType.IAP, Transaction.TransactionType.IAP_OFFCHAIN -> {
        txTypeIcon = R.drawable.ic_transaction_iab
      }
      Transaction.TransactionType.ADS, Transaction.TransactionType.ADS_OFFCHAIN -> {
        txTypeIcon = R.drawable.ic_transaction_poa
        currencySymbol = WalletCurrency.CREDITS.symbol
      }
      Transaction.TransactionType.BONUS -> {
        txTypeIcon = R.drawable.ic_transaction_peer
        isTypeIconVisible = false
        address = context.getString(R.string.transaction_type_bonus)
        currencySymbol = WalletCurrency.CREDITS.symbol
      }
      Transaction.TransactionType.TOP_UP -> {
        txTypeIcon = R.drawable.transaction_type_top_up
        isTypeIconVisible = false
        address = context.getString(R.string.topup_home_button)
        currencySymbol = WalletCurrency.CREDITS.symbol
      }
      Transaction.TransactionType.TRANSFER_OFF_CHAIN -> {
        txTypeIcon = R.drawable.ic_chain
        isTypeIconVisible = true
        description = context.getString(R.string.transaction_type_p2p)
        currencySymbol = WalletCurrency.CREDITS.symbol
      }
      Transaction.TransactionType.TRANSFER -> {
        txTypeIcon = R.drawable.ic_chain
        description = context.getString(R.string.transaction_type_p2p)
        isTypeIconVisible = true
        currencySymbol = when (tx.method) {
          Transaction.Method.UNKNOWN,
          Transaction.Method.APPC_C -> WalletCurrency.CREDITS.symbol
          Transaction.Method.APPC -> WalletCurrency.APPCOINS.symbol
          Transaction.Method.ETH -> WalletCurrency.ETHEREUM.symbol
        }
      }
      Transaction.TransactionType.ETHER_TRANSFER -> {
        txTypeIcon = R.drawable.ic_chain
        description = context.getString(R.string.transaction_type_p2p)
        isTypeIconVisible = true
        currencySymbol = WalletCurrency.ETHEREUM.symbol
      }
      Transaction.TransactionType.BONUS_REVERT -> {
        address = context.getString(R.string.transaction_type_reverted_bonus_title)
        holder.setRevertMessage(tx.linkedTx)
        currencySymbol = WalletCurrency.CREDITS.symbol
      }
      Transaction.TransactionType.TOP_UP_REVERT -> {
        address = context.getString(R.string.transaction_type_reverted_topup_title)
        holder.setRevertMessage(tx.linkedTx)
        currencySymbol = WalletCurrency.CREDITS.symbol
      }
      Transaction.TransactionType.IAP_REVERT -> {
        address = context.getString(R.string.transaction_type_reverted_purchase_title)
        holder.setRevertMessage(tx.linkedTx)
      }
      Transaction.TransactionType.SUBS_OFFCHAIN -> {
        txTypeIcon = R.drawable.ic_transaction_subscription
        isTypeIconVisible = true
      }
      Transaction.TransactionType.ESKILLS_REWARD -> {
        address = context.getString(R.string.transaction_type_eskills_reward)
      }
      Transaction.TransactionType.ESKILLS -> {
        txTypeIcon = R.drawable.ic_transaction_iab
      }
      else -> Unit
    }

    holder.setIcons(uri, txTypeIcon, isTypeIconVisible)
    holder.setDescription(description)
    holder.setAddress(address)
    holder.setValues(currencySymbol!!, isSent)
  }

  private fun TransactionHolder.setAddress(text: String?) {
    address.text = text
  }

  private fun TransactionHolder.setDescription(desc: String) {
    description.text = desc
  }

  private fun TransactionHolder.setValues(currencySymbol: String, isSent: Boolean) {
    var valueStr = tx.value
    val flipSign = if (isRevert(tx.type)) true else isSent

    valueStr = getScaledValue(valueStr, C.ETHER_DECIMALS.toLong(), currencySymbol, flipSign)
    valueStr = if (valueStr == "0") "0 " else valueStr

    if (shouldShowFiat(tx.paidAmount, tx.paidCurrency)) {
      val paidAmount = getScaledValue(tx.paidAmount!!, 0, "", flipSign)
      paidValue.text = paidAmount
      paidCurrency.text = tx.paidCurrency
      valueTextView.visibility = View.VISIBLE
      currency.visibility = View.VISIBLE
      valueTextView.text = valueStr
      currency.text = currencySymbol
    } else {
      paidValue.text = valueStr
      paidCurrency.text = currencySymbol
      valueTextView.visibility = View.GONE
      currency.visibility = View.GONE
    }

    currency.text = currencySymbol
    valueTextView.text = valueStr
  }

  private fun TransactionHolder.setIcons(uri: String?, txTypeIcon: Int,
                                         txTypeIconVisible: Boolean) {
    typeIconLayout.visibility = if (txTypeIconVisible) View.VISIBLE else View.GONE

    GlideApp.with(itemView.context)
        .load(uri)
        .apply(RequestOptions.bitmapTransform(CircleCrop())
            .error(txTypeIcon))
        .listener(object : RequestListener<Drawable?> {
          override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable?>,
                                    isFirstResource: Boolean): Boolean {
            typeIconLayout.visibility = View.GONE
            return false
          }

          override fun onResourceReady(resource: Drawable?, model: Any, target: Target<Drawable?>,
                                       dataSource: DataSource, isFirstResource: Boolean): Boolean {
            typeIconImageView.setImageResource(txTypeIcon)
            return false
          }
        })
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(srcImage)
  }

  private fun getDefaultAddress(context: Context, isSent: Boolean): String {
    val details = tx.details
    if (details != null) {
      return if (details.sourceName == null) {
        if (isSent) tx.to else tx.from
      } else {
        if (tx.type == Transaction.TransactionType.BONUS) {
          context.getString(R.string.gamification_transaction_title, details.sourceName)
        } else {
          details.sourceName ?: ""
        }
      }
    }
    return ""
  }

  private fun isRevert(type: Transaction.TransactionType): Boolean {
    return type == Transaction.TransactionType.IAP_REVERT
        || type == Transaction.TransactionType.TOP_UP_REVERT
        || type == Transaction.TransactionType.BONUS_REVERT
  }

  private fun shouldShowFiat(paidAmount: String?, paidCurrency: String?): Boolean {
    return (paidAmount != null && paidCurrency != "APPC" && paidCurrency != "APPC-C" && paidCurrency != "ETH")
  }

  private fun getScaledValue(valueStr: String, decimals: Long, currencySymbol: String,
                             flipSign: Boolean): String {
    val sign = if (flipSign) -1 else 1
    val walletCurrency = WalletCurrency.mapToWalletCurrency(currencySymbol);
    val value = BigDecimal(valueStr).divide(BigDecimal(10.toDouble()
        .pow(decimals.toDouble())))
        .multiply(sign.toBigDecimal())
    // In case of positive value, we need to explicitly put the "+" sign
    val signedString = if (value > BigDecimal.ZERO) "+" else ""
    return signedString + formatter!!.formatCurrency(value, walletCurrency)
  }

  private fun TransactionHolder.setRevertMessage(linkedTxs: List<Transaction>?) {
    var message: String? = null
    if (linkedTxs == null || linkedTxs.isEmpty()) {
      revertMessage.visibility = View.GONE
    } else {
      val linkedTx = linkedTxs[0]
      when (tx.type) {
        Transaction.TransactionType.BONUS_REVERT -> {
          message = itemView.context.getString(R.string.transaction_type_reverted_bonus_body,
              getDate(linkedTx.timeStamp))
        }
        Transaction.TransactionType.IAP_REVERT -> {
          message =
              itemView.context.getString(R.string.transaction_type_reverted_purchase_body,
                  getDate(linkedTx.timeStamp))
        }
        Transaction.TransactionType.TOP_UP_REVERT -> {
          message = itemView.context.getString(R.string.transaction_type_reverted_topup_body,
              getDate(linkedTx.timeStamp))
        }
      }
      revertMessage.text = message
      revertMessage.visibility = View.VISIBLE
    }
  }

  private fun getDate(timeStampInSec: Long): String {
    val cal = Calendar.getInstance(Locale.getDefault())
    cal.timeInMillis = timeStampInSec
    return DateFormat.format("MMM, dd yyyy", cal.time)
        .toString()
  }

  private fun isTypeIconVisibleBasedOnDescription(details: TransactionDetails?,
                                                  uri: String?): Boolean {
    return !(uri == null || details?.sourceName == null)
  }

  class TransactionHolder : BaseViewHolder() {
    val srcImage by bind<ImageView>(R.id.img)
    val typeIconLayout by bind<View>(R.id.type_icon)
    val typeIconImageView by bind<ImageView>(R.id.type_icon_image_view)
    val address by bind<TextView>(R.id.address)
    val description by bind<TextView>(R.id.description)
    val valueTextView by bind<TextView>(R.id.value)
    val currency by bind<TextView>(R.id.currency)
    val paidValue by bind<TextView>(R.id.paid_value)
    val paidCurrency by bind<TextView>(R.id.paid_currency)
    val revertMessage by bind<TextView>(R.id.revert_message)
  }
}
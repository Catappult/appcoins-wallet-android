package com.asfoundation.wallet.transactions

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import com.appcoins.wallet.core.network.backend.model.StatusResponse
import com.appcoins.wallet.core.network.backend.model.TransactionResponse
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse
import com.appcoins.wallet.core.utils.android_common.AmountUtils.format18decimals
import com.appcoins.wallet.core.utils.android_common.AmountUtils.formatMoney
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_green
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_red
import com.asf.wallet.R
import com.asfoundation.wallet.transactions.StatusType.PENDING
import com.asfoundation.wallet.transactions.StatusType.REJECTED
import com.asfoundation.wallet.transactions.StatusType.SUCCESS
import kotlinx.parcelize.Parcelize
import java.util.Currency

const val CURRENCY_CODE_LENGTH = 3

@Parcelize
data class TransactionModel(
  val orderId: String?,
  val status: StatusType,
  val type: TransactionTypeResponse,
  val date: String,
  val amount: String,
  val app: String?,
  val description: String?,
  val appIcon: String?,
  val amountSubtitle: String,
  val from: String,
  val to: String,
  val sku: String?,
  val txId: String?,
  val invoiceId: String?,
  val method: String?,
  val partialPayment: PartialPaymentModel?
) : Parcelable {
  companion object {
    val METHOD_SANDBOX = "sandbox"
  }
}

/**
 * The two rails a partial payment was funded from: part of the purchase came out of the AppCoins
 * balance, the rest was charged to [TransactionModel.method].
 *
 * Amounts are already formatted and are expressed in the currency the purchase was paid in - the
 * backend does not convert the split into the wallet's selected currency, so these can carry a
 * different symbol than [TransactionModel.amount].
 */
@Parcelize
data class PartialPaymentModel(
  val creditsAmount: String,
  val creditsAppcAmount: String,
  val chargedAmount: String?,
  val creditsWeight: Float
) : Parcelable

fun TransactionResponse.toModel(selectedCurrency: String): TransactionModel {
  return TransactionModel(
    orderId = reference,
    status = status.toStatusType(),
    type = type,
    date = processedTime,
    app = app,
    appIcon = appIcon,
    description = bonusDescription,
    amount = paidAmount(selectedCurrency) ?: amount(),
    amountSubtitle =
    if (paidAmount(selectedCurrency) == null) amountCurrency
    else "${amount()} $amountCurrency",
    from = sender,
    to = receiver,
    sku = sku,
    txId = txId,
    invoiceId = invoiceId,
    method = method,
    partialPayment = partialPaymentModel()
  )
}

enum class StatusType(val description: Int, val color: Color) {
  SUCCESS(description = R.string.transaction_status_success, color = styleguide_green),
  REJECTED(description = R.string.transaction_status_rejected, color = styleguide_red),
  PENDING(description = R.string.transaction_status_pending, color = styleguide_light_grey)
}

fun StatusResponse.toStatusType() =
  when (this) {
    StatusResponse.SUCCESS -> SUCCESS
    StatusResponse.REJECTED -> REJECTED
    else -> PENDING
  }

private fun TransactionResponse.paidAmount(selectedCurrency: String): String? {
  val sign = type.sign
  return defaultCurrencyAmount.formatMoney(selectedCurrency.currencySymbol(), sign)
    ?: paidCurrencyAmount.formatMoney(paidCurrency.currencySymbol(), sign)
}

private fun TransactionResponse.amount() = amount.format18decimals(sign = type.sign)

private fun TransactionResponse.partialPaymentModel(): PartialPaymentModel? {
  val partial = partialCreditsPayment ?: return null
  val currencySymbol = paidCurrency.currencySymbol()
  val credits = partial.paidCurrencyAmount.toBigDecimalOrNull() ?: return null
  val charged = partial.chargedAmount?.toBigDecimalOrNull()
  val total = charged?.let { credits + it }
  return PartialPaymentModel(
    creditsAmount = partial.paidCurrencyAmount.formatMoney(currencySymbol) ?: return null,
    creditsAppcAmount = "${partial.appcAmount.format18decimals()} $amountCurrency",
    chargedAmount = partial.chargedAmount.formatMoney(currencySymbol),
    creditsWeight =
    if (total == null || total.signum() <= 0) 1f
    else (credits.toFloat() / total.toFloat()).coerceIn(0f, 1f)
  )
}

fun String?.currencySymbol(): String =
  if (this != null && this.length == CURRENCY_CODE_LENGTH) Currency.getInstance(this).symbol
  else ""

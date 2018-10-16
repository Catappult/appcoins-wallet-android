package com.appcoins.wallet.billing

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.appcoins.wallet.billing.AppcoinsBillingBinder.Companion.EXTRA_BDS_IAP
import com.appcoins.wallet.billing.AppcoinsBillingBinder.Companion.EXTRA_DEVELOPER_PAYLOAD
import com.appcoins.wallet.billing.repository.entity.SKU
import com.appcoins.wallet.billing.repository.entity.TransactionData
import com.appcoins.wallet.billing.util.PayloadHelper
import com.google.gson.Gson
import org.spongycastle.util.encoders.Hex
import java.io.UnsupportedEncodingException
import java.math.BigDecimal
import java.util.*


class BillingIntentBuilder(val context: Context) {

  @Throws(Exception::class)
  fun buildBuyIntentBundle(sku: SKU, tokenContractAddress: String,
                           iabContractAddress: String, payload: String, bdsIap: Boolean, packageName: String): Bundle {
    val result = Bundle()

    val intent =
        buildPaymentIntent(sku, tokenContractAddress, iabContractAddress, payload, bdsIap, packageName)

    result.putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_OK)
    result.putParcelable(AppcoinsBillingBinder.BUY_INTENT, intent)

    return result
  }

  private fun buildPaymentIntent(sku: SKU, tokenContractAddress: String,
                                 iabContractAddress: String, payload: String,
                                 bdsIap: Boolean, packageName: String): PendingIntent {

    val amount = BigDecimal(sku.amount)
    val value = amount.multiply(BigDecimal.TEN.pow(18))

    val intent = Intent(Intent.ACTION_VIEW)
    val data = Uri.parse(buildUriString(tokenContractAddress, iabContractAddress, value,
        PayloadHelper.getAddress(payload), sku.productId, BuildConfig.NETWORK_ID, packageName))
    intent.data = data

    intent.putExtra(AppcoinsBillingBinder.PRODUCT_NAME, sku.title)
    intent.putExtra(EXTRA_DEVELOPER_PAYLOAD, payload)
    intent.putExtra(EXTRA_BDS_IAP, bdsIap)

    return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
  }

  private fun buildUriString(tokenContractAddress: String, iabContractAddress: String,
                             amount: BigDecimal, developerAddress: String, skuId: String,
                             networkId: Int, packageName: String): String {

    val stringBuilder = StringBuilder(4)
    try {
      Formatter(stringBuilder).use { formatter ->
        formatter.format("ethereum:%s@%d/buy?uint256=%s&address=%s&data=%s&iabContractAddress=%s",
            tokenContractAddress, networkId, amount.toString(), developerAddress,
            buildUriData(skuId, packageName), iabContractAddress)
      }
    } catch (e: UnsupportedEncodingException) {
      throw RuntimeException("UTF-8 not supported!", e)
    }
    return stringBuilder.toString()
  }

  private fun buildUriData(skuId: String, packageName: String): String {
    return "0x" + Hex.toHexString(
        Gson().toJson(TransactionData(type = "INAPP", domain = packageName, skuId = skuId)).toByteArray(
            charset("UTF-8")))
  }
}
package com.appcoins.wallet.billing

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.appcoins.wallet.billing.AppcoinsBillingBinder.Companion.EXTRA_BDS_IAP
import com.appcoins.wallet.billing.AppcoinsBillingBinder.Companion.EXTRA_DEVELOPER_PAYLOAD
import com.appcoins.wallet.billing.repository.entity.TransactionData
import com.appcoins.wallet.billing.util.PayloadHelper
import com.google.gson.Gson
import org.spongycastle.util.encoders.Hex
import java.io.UnsupportedEncodingException
import java.util.*


class BillingIntentBuilder(val context: Context) {

  @Throws(Exception::class)
  fun buildBuyIntentBundle(tokenContractAddress: String, iabContractAddress: String,
                           payload: String?, bdsIap: Boolean, packageName: String,
                           developerAddress: String?, skuId: String): Bundle {

    val intent =
        buildPaymentIntent(tokenContractAddress, iabContractAddress, developerAddress, skuId,
            packageName, payload, bdsIap)
    return Bundle().apply {
      val pendingIntent = buildPaymentPendingIntent(intent)

      putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_OK)
      putParcelable(AppcoinsBillingBinder.BUY_INTENT, pendingIntent)
      putParcelable(AppcoinsBillingBinder.BUY_INTENT_RAW, intent)
    }
  }

  private fun buildPaymentPendingIntent(intent: Intent): PendingIntent {
    return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
  }

  private fun buildPaymentIntent(tokenContractAddress: String,
                                 iabContractAddress: String, developerAddress: String?,
                                 skuId: String, packageName: String, payload: String?,
                                 bdsIap: Boolean): Intent {
    val uri = Uri.parse(buildUriString(tokenContractAddress, iabContractAddress,
        developerAddress, skuId, BuildConfig.NETWORK_ID, packageName,
        PayloadHelper.getPayload(payload), PayloadHelper.getOrderReference(payload),
        PayloadHelper.getOrigin(payload)))


    return Intent(Intent.ACTION_VIEW).apply {
      data = uri
      putExtra(EXTRA_DEVELOPER_PAYLOAD, payload)
      putExtra(EXTRA_BDS_IAP, bdsIap)
      setPackage(context.packageName)
    }
  }

  private fun buildUriString(tokenContractAddress: String, iabContractAddress: String,
                             developerAddress: String?, skuId: String, networkId: Int,
                             packageName: String, developerPayload: String?,
                             orderReference: String?, origin: String?): String {
    val stringBuilder = StringBuilder(4)
    try {
      Formatter(stringBuilder).use { formatter ->
        formatter.format("ethereum:%s@%d/buy?address=%s&data=%s&iabContractAddress=%s",
            tokenContractAddress, networkId, developerAddress ?: "",
            buildUriData(skuId, packageName, developerPayload, orderReference, origin),
            iabContractAddress)
      }
    } catch (e: UnsupportedEncodingException) {
      throw RuntimeException("UTF-8 not supported!", e)
    }
    return stringBuilder.toString()
  }

  private fun buildUriData(type: String, skuId: String, packageName: String,
                           developerPayload: String?, orderReference: String?, origin: String?,
                           subPeriod: String?, trialPeriod: String?,
                           introAppcAmount: BigDecimal?, introPeriod: String?,
                           introCycles: Int?): String {
    return "0x" + Hex.toHexString(Gson().toJson(
        TransactionData(type.toUpperCase(Locale.ROOT), packageName, skuId, developerPayload,
            orderReference,
            origin, subPeriod, trialPeriod, introAppcAmount?.toString(), introPeriod,
            introCycles?.toString()))
        .toByteArray(charset("UTF-8")))
  }
}
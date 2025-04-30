package com.appcoins.wallet.billing

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.net.toUri
import com.appcoins.wallet.billing.AppcoinsBillingBinder.Companion.EXTRA_BDS_IAP
import com.appcoins.wallet.billing.AppcoinsBillingBinder.Companion.EXTRA_DEVELOPER_PAYLOAD
import com.appcoins.wallet.billing.repository.entity.TransactionData
import com.appcoins.wallet.billing.util.PayloadHelper
import com.appcoins.wallet.core.utils.properties.MiscProperties
import com.google.gson.Gson
import org.spongycastle.util.encoders.Hex
import java.io.UnsupportedEncodingException
import java.math.BigDecimal
import java.util.*

class BillingIntentBuilder(val context: Context) {

  @Throws(Exception::class)
  fun buildBuyIntentBundle(
    type: String, tokenContractAddress: String,
    iabContractAddress: String,
    payload: String?, bdsIap: Boolean,
    packageName: String,
    skuId: String,
    appcAmount: BigDecimal,
    skuTitle: String,
    subscriptionPeriod: String?,
    trialPeriod: String?,
    oemid: String?,
    guestWalletId: String?,
    freeTrialDuration: String?,
    subscriptionStartingDate: String?,
  ): Bundle {
    val intent = buildPaymentIntent(
      type = type,
      amount = appcAmount,
      tokenContractAddress = tokenContractAddress,
      iabContractAddress = iabContractAddress,
      skuId = skuId,
      packageName = packageName,
      payload = payload,
      skuTitle = skuTitle,
      bdsIap = bdsIap,
      subscriptionPeriod = subscriptionPeriod,
      trialPeriod = trialPeriod,
      oemid = oemid,
      guestWalletId = guestWalletId,
      freeTrialDuration = freeTrialDuration,
      subscriptionStartingDate = subscriptionStartingDate
    )
    return Bundle().apply {
      val pendingIntent = buildPaymentPendingIntent(intent)

      putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_OK)
      putParcelable(AppcoinsBillingBinder.BUY_INTENT, pendingIntent)
      putParcelable(AppcoinsBillingBinder.BUY_INTENT_RAW, intent)
    }
  }

  private fun buildPaymentPendingIntent(intent: Intent): PendingIntent {
    return PendingIntent.getActivity(
      context,
      0,
      intent,
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      else
        PendingIntent.FLAG_UPDATE_CURRENT
    )
  }

  private fun buildPaymentIntent(
    type: String,
    amount: BigDecimal,
    tokenContractAddress: String,
    iabContractAddress: String,
    skuId: String,
    packageName: String,
    payload: String?,
    skuTitle: String,
    bdsIap: Boolean,
    subscriptionPeriod: String?,
    trialPeriod: String?,
    oemid: String?,
    guestWalletId: String?,
    freeTrialDuration: String?,
    subscriptionStartingDate: String?
  ): Intent {
    val value = amount.multiply(BigDecimal.TEN.pow(18))
    val uri = buildUriString(
      type = type,
      tokenContractAddress = tokenContractAddress,
      iabContractAddress = iabContractAddress,
      amount = value,
      skuId = skuId,
      networkId = MiscProperties.NETWORK_ID,
      packageName = packageName,
      developerPayload = PayloadHelper.getPayload(payload),
      orderReference = PayloadHelper.getOrderReference(payload),
      origin = PayloadHelper.getOrigin(payload),
      subscriptionPeriod = subscriptionPeriod,
      trialPeriod = trialPeriod,
      oemid = oemid,
      guestWalletId = guestWalletId,
      externalBuyerReference = PayloadHelper.getExternalBuyerReference(payload),
      isFreeTrial = PayloadHelper.isFreeTrial(payload),
      freeTrialDuration = freeTrialDuration,
      subscriptionStartingDate = subscriptionStartingDate
    ).toUri()


    return Intent(Intent.ACTION_VIEW).apply {
      data = uri
      putExtra(AppcoinsBillingBinder.PRODUCT_NAME, skuTitle)
      putExtra(EXTRA_DEVELOPER_PAYLOAD, payload)
      putExtra(EXTRA_BDS_IAP, bdsIap)
      setPackage(context.packageName)
    }
  }

  private fun buildUriString(
    type: String, tokenContractAddress: String,
    iabContractAddress: String,
    amount: BigDecimal,
    skuId: String,
    networkId: Int, packageName: String,
    developerPayload: String?,
    orderReference: String?, origin: String?,
    subscriptionPeriod: String?, trialPeriod: String?,
    oemid: String?,
    guestWalletId: String?,
    externalBuyerReference: String?,
    isFreeTrial: Boolean?,
    freeTrialDuration: String?,
    subscriptionStartingDate: String?
  ): String {
    val stringBuilder = StringBuilder(4)
    try {
      Formatter(stringBuilder).use { formatter ->
        formatter.format(
          "ethereum:%s@%d/buy?uint256=%s&address=%s&data=%s&iabContractAddress=%s",
          tokenContractAddress, networkId, amount.toString(), "",
          buildUriData(
            type = type,
            skuId = skuId,
            packageName = packageName,
            developerPayload = developerPayload,
            orderReference = orderReference,
            origin = origin,
            subscriptionPeriod = subscriptionPeriod,
            trialPeriod = trialPeriod,
            oemid = oemid,
            guestWalletId = guestWalletId,
            externalBuyerReference = externalBuyerReference,
            isFreeTrial = isFreeTrial,
            freeTrialDuration = freeTrialDuration,
            subscriptionStartingDate = subscriptionStartingDate
          ),
          iabContractAddress
        )
      }
    } catch (e: UnsupportedEncodingException) {
      throw RuntimeException("UTF-8 not supported!", e)
    }
    return stringBuilder.toString()
  }

  private fun buildUriData(
    type: String, skuId: String, packageName: String,
    developerPayload: String?, orderReference: String?,
    origin: String?,
    subscriptionPeriod: String?,
    trialPeriod: String?,
    oemid: String?,
    guestWalletId: String?,
    externalBuyerReference: String?,
    isFreeTrial: Boolean?,
    freeTrialDuration: String?,
    subscriptionStartingDate: String?
  ): String {
    return "0x" + Hex.toHexString(
      Gson().toJson(
        TransactionData(
          _type = type.toUpperCase(Locale.ROOT),
          _domain = packageName,
          _skuId = skuId,
          _payload = developerPayload,
          _orderReference = orderReference,
          _origin = origin,
          _period = subscriptionPeriod,
          _trialPeriod = trialPeriod,
          _oemId = oemid,
          _guestWalletId = guestWalletId,
          _externalBuyerReference = externalBuyerReference,
          _isFreeTrial = isFreeTrial,
          _freeTrialDuration = freeTrialDuration,
          _subscriptionStartingDate = subscriptionStartingDate
        )
      )
        .toByteArray(charset("UTF-8"))
    )
  }
}
package com.asfoundation.wallet.ui.webview_payment.models

import android.app.Activity
import android.content.Intent
import android.util.Log
import org.json.JSONObject


object WebPaymentSuccessParser {

  private const val TAG = "WebPaymentSuccessParser"

  private object JsonKeys {
    const val RESPONSE_CODE = "responseCode"
    const val PURCHASE_DATA = "purchaseData"
    const val DATA_SIGNATURE = "dataSignature"
    const val ORDER_REFERENCE = "orderReference"
    const val PAYMENT_METHOD = "paymentMethod"
    const val IS_STORED_CARD = "isStoredCard"
    const val WAS_CVC_REQUIRED = "wasCvcRequired"
    const val UID = "uid"
    const val HASH = "hash"
  }

  object Extras {
    const val RESPONSE_CODE = "RESPONSE_CODE"
    const val INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA"
    const val INAPP_DATA_SIGNATURE = "INAPP_DATA_SIGNATURE"
    const val INAPP_PURCHASE_ID = "INAPP_PURCHASE_ID"
    const val ORDER_REFERENCE = "ORDER_REFERENCE"
    const val PAYMENT_METHOD = "PAYMENT_METHOD"
    const val IS_STORED_CARD = "IS_STORED_CARD"
    const val WAS_CVC_REQUIRED = "WAS_CVC_REQUIRED"
    const val SKU_TYPE = "SKU_TYPE"
    const val UID = "UID"
    const val HASH = "HASH"

    const val PARSER_ERROR = "PARSER_ERROR"
    const val RAW_JSON = "RAW_JSON"
  }

  data class WebResponse(
    val responseCode: Int,
    val purchaseData: PurchaseData?,
    val dataSignature: String?,
    val orderReference: String?,
    val paymentMethod: String?,
    val isStoredCard: Boolean?,
    val wasCvcRequired: Boolean?,
    val uid: String?,
    val hash: String?
  ) {
    companion object {
      fun fromJson(json: String): WebResponse {
        val root = JSONObject(json)

        val pdObj = root.optJSONObject(JsonKeys.PURCHASE_DATA) ?: run {
          val asString = root.optString(JsonKeys.PURCHASE_DATA, "")
          if (asString.isNotEmpty()) JSONObject(asString) else null
        }
        val purchaseData = pdObj?.let { PurchaseData(it) }

        val responseCode = root.optInt(JsonKeys.RESPONSE_CODE, -1)
        val dataSignature = root.optString(JsonKeys.DATA_SIGNATURE).takeIf { it.isNotEmpty() }
        val orderReference = root.optString(JsonKeys.ORDER_REFERENCE).takeIf { it.isNotEmpty() }
        val paymentMethod = root.optString(JsonKeys.PAYMENT_METHOD).takeIf { it.isNotEmpty() }
        val isStoredCard =
          if (root.has(JsonKeys.IS_STORED_CARD)) root.optBoolean(JsonKeys.IS_STORED_CARD) else null
        val wasCvcRequired =
          if (root.has(JsonKeys.WAS_CVC_REQUIRED)) root.optBoolean(JsonKeys.WAS_CVC_REQUIRED) else null
        val uid = root.optString(JsonKeys.UID).takeIf { it.isNotEmpty() }
        val hash = root.optString(JsonKeys.HASH).takeIf { it.isNotEmpty() }

        return WebResponse(
          responseCode = responseCode,
          purchaseData = purchaseData,
          dataSignature = dataSignature,
          orderReference = orderReference,
          paymentMethod = paymentMethod,
          isStoredCard = isStoredCard,
          wasCvcRequired = wasCvcRequired,
          uid = uid,
          hash = hash
        )
      }
    }
  }

  data class Result(
    val resultCode: Int,
    val data: Intent
  )

  fun parse(json: String, skuType: String? = null): Result {
    return try {
      val web = WebResponse.fromJson(json)

      val intent = Intent().apply {
        putExtra(Extras.RESPONSE_CODE, web.responseCode)

        web.purchaseData?.toJson()?.let {
          putExtra(Extras.INAPP_PURCHASE_DATA, it)
        }

        web.dataSignature?.let {
          putExtra(Extras.INAPP_DATA_SIGNATURE, it)
        }

        web.purchaseData?.purchaseToken?.let {
          putExtra(Extras.INAPP_PURCHASE_ID, it)
        }

        web.orderReference?.let {
          putExtra(Extras.ORDER_REFERENCE, it)
        }

        (skuType ?: web.purchaseData?.productType)?.let {
          putExtra(Extras.SKU_TYPE, it)
        }

        web.paymentMethod?.let {
          putExtra(Extras.PAYMENT_METHOD, it)
        }
        web.isStoredCard?.let {
          putExtra(Extras.IS_STORED_CARD, it)
        }
        web.wasCvcRequired?.let {
          putExtra(Extras.WAS_CVC_REQUIRED, it)
        }

        web.uid?.let {
          putExtra(Extras.UID, it)
        }
        web.hash?.let {
          putExtra(Extras.HASH, it)
        }
      }

      val resultCode = when (web.responseCode) {
        0, 2, 3, 4, 5, 6, 7, 8 -> Activity.RESULT_OK
        1 -> Activity.RESULT_CANCELED
        else -> Activity.RESULT_FIRST_USER
      }

      Result(resultCode, intent)
    } catch (t: Throwable) {
      Log.e(TAG, "Failed to parse web response: ${t.message}", t)
      Result(
        Activity.RESULT_FIRST_USER,
        Intent().apply {
          putExtra(Extras.RESPONSE_CODE, -1)
          putExtra(Extras.PARSER_ERROR, t.message)
          putExtra(Extras.RAW_JSON, json)
        }
      )
    }
  }
}

data class PurchaseData(
  val orderId: String,
  val packageName: String,
  val productId: String,
  val productType: String,
  val purchaseTime: Long,
  val purchaseToken: String,
  val purchaseState: Int,
  val isAutoRenewing: Boolean,
  val developerPayload: String?,
  val obfuscatedExternalAccountId: String?,
) {
  constructor(json: JSONObject) : this(
    orderId = json.optString("orderId"),
    packageName = json.optString("packageName"),
    productId = json.optString("productId"),
    productType = json.optString("productType").takeIf { it.isNotEmpty() } ?: "INAPP",
    purchaseTime = json.optLong("purchaseTime"),
    purchaseToken = json.optString("purchaseToken"),
    purchaseState = json.optInt("purchaseState"),
    isAutoRenewing = json.optBoolean("isAutoRenewing"),
    developerPayload = json.optString("developerPayload").takeIf { it.isNotEmpty() },
    obfuscatedExternalAccountId = json.optString("obfuscatedExternalAccountId")
      .takeIf { it.isNotEmpty() }
  )

  fun toJson(): String =
    JSONObject().apply {
      put("orderId", orderId)
      put("packageName", packageName)
      put("productId", productId)
      put("purchaseTime", purchaseTime)
      put("purchaseToken", purchaseToken)
      put("purchaseState", purchaseState)

      developerPayload?.let { put("developerPayload", it) }
      obfuscatedExternalAccountId?.let { put("obfuscatedExternalAccountId", it) }
    }.toString()
}

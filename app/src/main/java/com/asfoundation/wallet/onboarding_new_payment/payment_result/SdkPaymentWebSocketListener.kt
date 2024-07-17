package com.asfoundation.wallet.onboarding_new_payment.payment_result

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

// Error mapping from sdk side
// Success - 0
// User pressed back or canceled a dialog - 1
// The network connection is down - 2
// This billing API version is not supported for the type requested - 3
// Requested SKU is not available for purchase - 4
// Invalid arguments provided to the API - 5
// Fatal error during the API action - 6
// Failure to purchase since item is already owned - 7
// Failure to consume since item is not owned - 8

class SdkPaymentWebSocketListener(
  private val purchaseToken: String?,
  private val orderId: String?,
  private val responseCode: Int
) : WebSocketListener() {
  override fun onOpen(webSocket: WebSocket, response: Response) {
    super.onOpen(webSocket, response)
    val data = mapOf(
      "purchaseToken" to purchaseToken,
      "orderId" to orderId,
      "responseCode" to responseCode
    )
    webSocket.send(JSONObject(data).toString())
  }

  override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
    super.onClosing(webSocket, code, reason)
    // Called when the connection is closing
    webSocket.close(1000, null)
  }

  companion object {
    const val SDK_STATUS_SUCCESS = 0
    const val SDK_STATUS_USER_CANCEL = 1
    const val SDK_STATUS_NETWORK_DOWN = 2
    const val SDK_STATUS_API_NOT_SUPPORTED = 3
    const val SDK_STATUS_SKU_NOT_AVAILABLE = 4
    const val SDK_STATUS_INVALID_ARGUMENTS = 5
    const val SDK_STATUS_FATAL_ERROR = 6
    const val SDK_STATUS_ITEM_ALREADY_OWNED = 7
    const val SDK_STATUS_ITEM_NOT_OWNED= 8
  }
}
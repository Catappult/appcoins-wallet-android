package com.asfoundation.wallet.onboarding_new_payment.payment_result

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
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

class SdkPaymentWebSocketListener(private val purchaseToken: String?, private val orderId: String?, private val responseCode: Int) : WebSocketListener() {
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
}
package com.asfoundation.wallet.billing.gameshub

import android.content.Context
import android.content.Intent
import android.util.Log
import com.appcoins.wallet.core.utils.properties.MiscProperties

class GamesHubBroadcastService {

  companion object {
    const val KEY_TXID = "txid"
    const val KEY_UID = "uid"
    const val KEY_PACKAGE_NAME = "package_name"
    const val KEY_USD_AMOUNT = "usd_amount"
    const val KEY_APPC_AMOUNT = "appc_amount"

    const val GAMES_HUB_BROADCAST = "GamesHubBroadcast"

    fun sendSuccessPaymentBroadcast(
      context: Context,
      uid: String,
      packageName: String,
      usdAmount: String,
      appcAmount: String
    ) {
      Log.d(
        GAMES_HUB_BROADCAST,
        "$KEY_UID = $uid, $KEY_PACKAGE_NAME = $packageName, $KEY_USD_AMOUNT = $usdAmount"
      )
      val intent = Intent(
        "${MiscProperties.GAMESHUB_PACKAGE}.${MiscProperties.GAMESHUB_BROADCAST_IAP_ACTION}"
      )
      intent.setPackage(MiscProperties.GAMESHUB_PACKAGE)
      intent.putExtra(KEY_UID, uid)
      intent.putExtra(KEY_PACKAGE_NAME, packageName)
      intent.putExtra(KEY_USD_AMOUNT, usdAmount)
//      intent.putExtra(KEY_APPC_AMOUNT, appcAmount)
      context.sendBroadcast(intent)
    }
  }

}

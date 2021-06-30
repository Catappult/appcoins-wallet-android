package cm.aptoide.skills.usecase

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import cm.aptoide.skills.BuildConfig
import cm.aptoide.skills.util.EskillsUri
import io.reactivex.Single
import java.math.BigDecimal

class PayTicketUseCase() {

  fun payTicket(
    ticketId: String, callbackUrl: String, productToken: String,
    ticketPrice: BigDecimal, priceCurrency: String, eskillsUri: EskillsUri, fragment: Fragment
  ): Single<Any> {
    return Single.fromCallable {
      val environment = eskillsUri.getEnvironment()
      if (environment == EskillsUri.MatchEnvironment.LIVE || environment == null) {
        launchPurchaseFlow(
          eskillsUri,
          callbackUrl,
          ticketId,
          productToken,
          ticketPrice,
          priceCurrency,
          fragment
        )
      }
      0
    }
  }

  private fun launchPurchaseFlow(
    eskillsUri: EskillsUri,
    callbackUrl: String,
    ticketId: String,
    productToken: String,
    ticketPrice: BigDecimal,
    priceCurrency: String,
    fragment: Fragment
  ) {
    val url: String =
      (BACKEND_HOST + "/transaction/inapp?domain=" + eskillsUri.getPackageName() + "&callback_url=" + callbackUrl +
          "&order_reference=" + ticketId + "&product_token=" + productToken + "&skills") + getOptionalFields(
        eskillsUri, ticketPrice, priceCurrency
      )

    val i = Intent(Intent.ACTION_VIEW)
    i.data = Uri.parse(url)
    i.setPackage(IAB_BIND_PACKAGE)

    var flags = PendingIntent.FLAG_UPDATE_CURRENT
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
      flags = flags or PendingIntent.FLAG_IMMUTABLE
    }

    val intent =
      PendingIntent.getActivity(
        fragment.requireContext()
          .getApplicationContext(), 0, i,
        flags
      )
    try {
      fragment.startIntentSenderForResult(
        intent.intentSender, RC_ONE_STEP, Intent(), 0, 0, 0,
        null
      )
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun getOptionalFields(
    eskillsUri: EskillsUri,
    ticketPrice: BigDecimal,
    priceCurrency: String
  ): String {
    var url = ""

    if (eskillsUri.getProduct() != null) {
      url = url + "&product=" + eskillsUri.getProduct()
    }

    if (eskillsUri.getPrice() != null) {
      url = url + "&value=" + eskillsUri.getPrice()
    } else {
      url = url + "&value=" + ticketPrice
    }

    if (eskillsUri.getCurrency() != null) {
      url = url + "&currency=" + eskillsUri.getCurrency()
    } else {
      url = url + "&currency=" + priceCurrency
    }

    return url
  }


  companion object {
    const val BACKEND_HOST = BuildConfig.BASE_HOST
    const val IAB_BIND_PACKAGE = BuildConfig.WALLET_PACKAGE
    const val RC_ONE_STEP = 10003
  }
}
package cm.aptoide.skills.usecase

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import cm.aptoide.skills.BuildConfig
import cm.aptoide.skills.model.CreatedTicket
import cm.aptoide.skills.model.Ticket
import cm.aptoide.skills.util.EskillsPaymentData
import io.reactivex.Single
import javax.inject.Inject

class SkillsNavigator @Inject constructor(){

  fun navigateToPayTicket(
      ticket: CreatedTicket, eskillsPaymentData: EskillsPaymentData, fragment: Fragment
  ): Single<Ticket> {
    val environment = eskillsPaymentData.environment
    if (environment == EskillsPaymentData.MatchEnvironment.LIVE || environment == null) {
      launchPurchaseFlow(eskillsPaymentData, ticket, fragment)
    }
    return Single.just(ticket)
  }

  private fun launchPurchaseFlow(
      eskillsPaymentData: EskillsPaymentData, ticket: CreatedTicket, fragment: Fragment
  ) {
    val url: String =
        (BACKEND_HOST + "/transaction/inapp?domain=" + eskillsPaymentData.packageName +
            "&callback_url=" + ticket.callbackUrl +
            "&order_reference=" + ticket.ticketId +
            "&product_token=" + ticket.productToken +
            "&skills") + getOptionalFields(eskillsPaymentData, ticket)

    val i = Intent(Intent.ACTION_VIEW)
    i.data = Uri.parse(url)
    i.setPackage(IAB_BIND_PACKAGE)

    var flags = PendingIntent.FLAG_UPDATE_CURRENT
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
      flags = flags or PendingIntent.FLAG_IMMUTABLE
    }

    val intent =
        PendingIntent.getActivity(
            fragment.requireContext().applicationContext, 0, i, flags
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
      eskillsPaymentData: EskillsPaymentData, ticket: CreatedTicket
  ): String {
    var url = ""

    if (eskillsPaymentData.product != null) {
      url = url + "&product=" + eskillsPaymentData.product
    }

    if (eskillsPaymentData.price != null) {
      url = url + "&value=" + eskillsPaymentData.price
    } else {
      url = url + "&value=" + ticket.ticketPrice
    }

    if (eskillsPaymentData.currency != null) {
      url = url + "&currency=" + eskillsPaymentData.currency
    } else {
      url = url + "&currency=" + ticket.priceCurrency
    }

    return url
  }


  companion object {
    const val BACKEND_HOST = BuildConfig.BASE_HOST
    const val IAB_BIND_PACKAGE = BuildConfig.WALLET_PACKAGE
    const val RC_ONE_STEP = 10003
  }
}
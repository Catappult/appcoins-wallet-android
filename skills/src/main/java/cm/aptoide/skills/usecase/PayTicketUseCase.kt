package cm.aptoide.skills.usecase

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import cm.aptoide.skills.BuildConfig
import cm.aptoide.skills.repository.TicketRepository
import cm.aptoide.skills.util.EskillsUri
import io.reactivex.Single

class PayTicketUseCase(private val ticketRepository: TicketRepository) {

  fun payTicket(ticketId: String, callbackUrl: String, productToken: String,
                eskillsUri: EskillsUri, fragment: Fragment): Single<Any> {
    return Single.fromCallable {
      val url: String =
          (BACKEND_HOST + "/transaction/inapp?product=" + eskillsUri.getProduct() +
              "&value=" + eskillsUri.getPrice() + "&currency=" + eskillsUri.getCurrency() +
              "&domain=" + eskillsUri.getPackageName() + "&callback_url=" + callbackUrl +
              "&order_reference=" + ticketId + "&product_token=" + productToken + "&skills")

      val i = Intent(Intent.ACTION_VIEW)
      i.data = Uri.parse(url)
      i.setPackage(IAB_BIND_PACKAGE)

      val intent =
          PendingIntent.getActivity(fragment.requireContext()
              .getApplicationContext(), 0, i,
              PendingIntent.FLAG_UPDATE_CURRENT)
      try {
        fragment.startIntentSenderForResult(intent.intentSender, RC_ONE_STEP, Intent(), 0, 0, 0,
            null)
      } catch (e: Exception) {
        e.printStackTrace()
      }

      0
    }
  }

  companion object {
    const val BACKEND_HOST = BuildConfig.BASE_HOST
    const val IAB_BIND_PACKAGE = BuildConfig.WALLET_PACKAGE
    const val RC_ONE_STEP = 10003
  }
}
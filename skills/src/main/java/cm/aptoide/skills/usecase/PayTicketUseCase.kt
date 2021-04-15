package cm.aptoide.skills.usecase

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import cm.aptoide.skills.repository.TicketRepository
import io.reactivex.Single

class PayTicketUseCase(private val ticketRepository: TicketRepository) {

  fun payTicket(ticketId: String, callbackUrl: String, fragment: Fragment): Single<Any> {
    return Single.fromCallable {
      val packageName = "com.appcoins.eskills2048.dev"

      val url: String =
          (BACKEND_HOST + "transaction/inapp?product=antifreeze&value=1.5&currency=USD"
              + "&domain="
              + packageName + "&callback_url=" + callbackUrl)

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
    const val BACKEND_HOST = "https://apichain-dev.blockchainds.com/"
    const val IAB_BIND_PACKAGE = "com.appcoins.wallet.dev"
    const val RC_ONE_STEP = 10003
  }
}
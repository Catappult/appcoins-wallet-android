package cm.aptoide.skills

import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment
import cm.aptoide.skills.entity.UserData
import cm.aptoide.skills.model.TicketResponse
import cm.aptoide.skills.usecase.*
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


class SkillsViewModel(private val createTicketUseCase: CreateTicketUseCase,
                      private val payTicketUseCase: PayTicketUseCase,
                      private val getTicketUseCase: GetTicketUseCase,
                      private val getTicketRetryMillis: Long,
                      private val loginUseCase: LoginUseCase,
                      private val getApplicationInfoUseCase: GetApplicationInfoUseCase) {
  fun handleWalletCreationIfNeeded(): Observable<String> {
    return createTicketUseCase.getOrCreateWallet()
  }

  fun getRoom(userId: String, userName: String, packageName: String,
              fragment: Fragment): Observable<UserData> {
    return createTicketUseCase.createTicket(packageName, userId, userName)
        .flatMap { ticketResponse ->
          payTicketUseCase.payTicket(ticketResponse.ticketId, ticketResponse.callbackUrl,
              ticketResponse.ticketPrice, ticketResponse.priceCurrency, ticketResponse.productToken,
              packageName, fragment)
              .flatMap {
                val roomIdPresent = AtomicBoolean(false)

                getTicketUseCase.getTicket(ticketResponse.ticketId)
                    .doOnSuccess { checkRoomIdPresent(it, roomIdPresent) }
                    .delay(getTicketRetryMillis, TimeUnit.MILLISECONDS)
                    .repeatUntil { roomIdPresent.get() }
                    .skipWhile { !roomIdPresent.get() }
                    .flatMapSingle { ticketResponse ->
                      loginUseCase.login(ticketResponse.roomId!!)
                          .map { session ->
                            UserData(ticketResponse.userId, ticketResponse.roomId,
                                ticketResponse.walletAddress, session)
                          }
                    }
                    .singleOrError()
              }
        }
        .toObservable()
  }

  private fun checkRoomIdPresent(ticketResponse: TicketResponse, roomIdPresent: AtomicBoolean) {
    if (ticketResponse.roomId != null) {
      roomIdPresent.set(true)
    }
  }

  fun getPayTicketRequestCode(): Int {
    return PayTicketUseCase.RC_ONE_STEP
  }

  fun payTicketOnActivityResult(resultCode: Int, txHash: String?) {

  }

  fun getApplicationIcon(packageName: String): Drawable? {
    return getApplicationInfoUseCase.getApplicationIcon(packageName)
  }

  fun getApplicationName(packageName: String): String? {
    return getApplicationInfoUseCase.getApplicationName(packageName)
  }
}

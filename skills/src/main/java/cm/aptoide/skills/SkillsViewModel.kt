package cm.aptoide.skills

import androidx.fragment.app.Fragment
import cm.aptoide.skills.entity.UserData
import cm.aptoide.skills.model.TicketResponse
import cm.aptoide.skills.usecase.CreateTicketUseCase
import cm.aptoide.skills.usecase.GetTicketUseCase
import cm.aptoide.skills.usecase.LoginUseCase
import cm.aptoide.skills.usecase.PayTicketUseCase
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


class SkillsViewModel(private val createTicketUseCase: CreateTicketUseCase,
                      private val payTicketUseCase: PayTicketUseCase,
                      private val getTicketUseCase: GetTicketUseCase,
                      private val getTicketRetryMillis: Long,
                      private val loginUseCase: LoginUseCase) {

  fun getRoom(userId: String, fragment: Fragment): Observable<UserData> {
    return createTicketUseCase.createTicket(userId)
        .flatMap { ticketResponse ->
          payTicketUseCase.payTicket(ticketResponse.ticketId, ticketResponse.callbackUrl, fragment)
              .flatMap {
                val roomIdPresent = AtomicBoolean(false)

                getTicketUseCase.getTicket(ticketResponse.ticketId)
                    .doOnSuccess { checkRoomIdPresent(it, roomIdPresent) }
                    .delay(getTicketRetryMillis, TimeUnit.MILLISECONDS)
                    .repeatUntil({ roomIdPresent.get() })
                    .skipWhile({ !roomIdPresent.get() })
                    .flatMapSingle { ticketResponse ->
                      loginUseCase.login(ticketResponse.roomId!!)
                          .map { session ->
                            UserData(ticketResponse.userId, ticketResponse.roomId!!,
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
}

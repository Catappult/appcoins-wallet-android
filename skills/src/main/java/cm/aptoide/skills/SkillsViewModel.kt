package cm.aptoide.skills

import cm.aptoide.skills.entity.UserData
import cm.aptoide.skills.usecase.CreateTicketUseCase
import cm.aptoide.skills.usecase.GetRoomUseCase
import cm.aptoide.skills.usecase.PayTicketUseCase
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class SkillsViewModel(private val createTicketUseCase: CreateTicketUseCase,
                      private val payTicketUseCase: PayTicketUseCase,
                      private val getRoomUseCase: GetRoomUseCase,
                      private val getRoomRetryMillis: Long) {

  fun getRoom(userId: String): Observable<UserData> {
    return createTicketUseCase.createTicket(userId)
        .flatMap { ticketResponse ->
          payTicketUseCase.payTicket(ticketResponse.ticketId, ticketResponse.callbackUrl)
              .flatMap {
                getRoomUseCase.getRoom(ticketResponse.ticketId)
                    .retryWhen { t -> t.delay(getRoomRetryMillis, TimeUnit.MILLISECONDS) }
                    .map { roomResponse ->
                      UserData(ticketResponse.userId, roomResponse.roomId,
                          ticketResponse.walletAddress)
                    }
              }
        }
        .toObservable()
  }
}

package cm.aptoide.skills

import cm.aptoide.skills.entity.UserData
import cm.aptoide.skills.usecase.CreateTicketUseCase
import cm.aptoide.skills.usecase.GetRoomUseCase
import cm.aptoide.skills.usecase.PayTicketUseCase
import io.reactivex.Observable

class SkillsViewModel(private val createTicketUseCase: CreateTicketUseCase,
                      private val payTicketUseCase: PayTicketUseCase,
                      private val getRoomUseCase: GetRoomUseCase) {

  fun getRoom(userId: String): Observable<UserData> {
    return createTicketUseCase.createTicket(userId)
        .flatMap { ticketResponse ->
          payTicketUseCase.payTicket(ticketResponse.ticketId, ticketResponse.callbackUrl)
              .flatMapObservable {
                getRoomUseCase.getRoom(ticketResponse.ticketId)
                    .map { roomResponse ->
                      UserData(ticketResponse.userId, roomResponse.roomId,
                          ticketResponse.walletAddress)
                    }
              }
        }
  }
}

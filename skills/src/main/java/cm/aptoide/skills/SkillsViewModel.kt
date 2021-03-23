package cm.aptoide.skills

import cm.aptoide.skills.entity.UserData
import cm.aptoide.skills.usecase.CreateTicketUseCase
import cm.aptoide.skills.usecase.GetRoomUseCase
import io.reactivex.Observable

class SkillsViewModel(private val createTicketUseCase: CreateTicketUseCase,
                      private val getRoomUseCase: GetRoomUseCase) {

  fun getRoom(userId: String): Observable<UserData> {
    return createTicketUseCase.createTicket(userId)
        .flatMap { ticketResponse ->
          getRoomUseCase.getRoom(ticketResponse.ticketId)
              .map { roomResponse ->
                UserData(ticketResponse.userId, roomResponse.roomId, ticketResponse.walletAddress)
              }
        }
  }
}

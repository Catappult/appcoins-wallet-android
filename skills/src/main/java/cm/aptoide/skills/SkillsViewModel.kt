package cm.aptoide.skills

import cm.aptoide.skills.model.TicketResponse
import cm.aptoide.skills.usecase.CreateTicketUseCase
import io.reactivex.Observable

class SkillsViewModel(private val createTicketUseCase: CreateTicketUseCase) {

  fun createTicket(): Observable<TicketResponse> {
    return createTicketUseCase.createTicket()
  }
}

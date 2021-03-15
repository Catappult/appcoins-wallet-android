package cm.aptoide.skills

import cm.aptoide.skills.model.TicketResponse
import cm.aptoide.skills.repository.TicketsRepository
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class SkillsViewModel(val ticketsRepository: TicketsRepository) {

  fun createTicket(): Observable<TicketResponse> {
    return ticketsRepository.createTicket()
        .subscribeOn(Schedulers.io())
  }
}

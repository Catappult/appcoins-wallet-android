package cm.aptoide.skills.usecase

import cm.aptoide.skills.repository.TicketRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class PayTicketUseCase(private val ticketRepository: TicketRepository) {

  fun payTicket(ticketId: String, callbackUrl: String): Single<Any> {
    return ticketRepository.payTicket(ticketId, callbackUrl)
        .subscribeOn(Schedulers.io())
  }
}
package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.repository.TicketRepository
import io.reactivex.Single
import javax.inject.Inject

class UserFirstTimeCheckUseCase @Inject constructor (
  private val ewtObtainer: EwtObtainer,
  private val ticketRepository: TicketRepository,
){
  operator fun invoke(): Single<Boolean> {
    return ewtObtainer.getEWT()
      .flatMap {ticketRepository.getFirstTimeUserCheck(it)}
  }
}
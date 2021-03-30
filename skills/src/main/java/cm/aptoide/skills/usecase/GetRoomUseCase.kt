package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.RoomResponse
import cm.aptoide.skills.repository.RoomRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class GetRoomUseCase(private val walletAddressObtainer: WalletAddressObtainer,
                     private val ewtObtainer: EwtObtainer,
                     private val roomRepository: RoomRepository) {

  fun getRoom(ticketId: String): Single<RoomResponse> {
    return walletAddressObtainer.getWalletAddress()
        .flatMap { walletAddress ->
          ewtObtainer.getEWT()
              .flatMap { ewt -> roomRepository.getRoomByTicketId(ewt, ticketId, walletAddress) }
        }
        .subscribeOn(Schedulers.io())

  }
}
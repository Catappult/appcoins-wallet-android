package cm.aptoide.skills.usecase

import cm.aptoide.skills.repository.RoomRepository
import com.appcoins.wallet.core.network.eskills.model.RoomResponse
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GetRoomUseCase @Inject constructor(private val roomRepository: RoomRepository) {
  fun getRoom(session: String): Single<RoomResponse> {
    return roomRepository.getRoom(session)
      .subscribeOn(Schedulers.io())
  }
}
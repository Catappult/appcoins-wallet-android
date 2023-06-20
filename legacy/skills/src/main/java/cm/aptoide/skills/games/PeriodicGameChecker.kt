package cm.aptoide.skills.games

import com.appcoins.wallet.core.network.eskills.model.RoomStatus
import com.appcoins.wallet.core.network.eskills.model.User
import cm.aptoide.skills.repository.RoomRepository
import com.appcoins.wallet.core.network.eskills.model.RoomResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class PeriodicGameChecker(
  private val roomRepository: RoomRepository,
  private val getRoomPeriodSeconds: Long,
  private val gameStateListener: GameStateListener
) {
  private val disposables = CompositeDisposable()

  fun start(session: String) {
    disposables.add(Observable.interval(0, getRoomPeriodSeconds, TimeUnit.SECONDS)
      .flatMapSingle<Any> {
        roomRepository.getRoom(session)
          .observeOn(AndroidSchedulers.mainThread())
          .doOnSuccess { roomResponse: RoomResponse -> checkGameStatus(roomResponse as RoomResponse.SuccessfulRoomResponse) }
      }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun checkGameStatus(roomResponse: RoomResponse.SuccessfulRoomResponse) {
    when (roomResponse.status) {
      RoomStatus.PLAYING -> gameStateListener.onUpdate(GameUpdate(getUserNames(roomResponse)))
      RoomStatus.COMPLETED -> gameStateListener.onFinishGame(
        FinishedGame(roomResponse.roomResult.winnerAmount, isWinner(roomResponse))
      )
    }
  }

  private fun getUserNames(roomResponse: RoomResponse.SuccessfulRoomResponse): List<String> {
    val userNames = mutableListOf<String>()
    for (user: User in roomResponse.users) {
      userNames.add(user.userName)
    }
    return userNames
  }

  private fun isWinner(roomResponse: RoomResponse.SuccessfulRoomResponse): Boolean {
    return roomResponse.roomResult.winner.walletAddress
      .equals(roomResponse.currentUser.walletAddress, ignoreCase = true)
  }

  fun stop() = disposables.clear()
}

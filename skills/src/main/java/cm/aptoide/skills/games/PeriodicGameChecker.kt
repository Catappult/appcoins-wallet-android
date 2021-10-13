package cm.aptoide.skills.games

import cm.aptoide.skills.model.RoomResponse
import cm.aptoide.skills.model.RoomStatus
import cm.aptoide.skills.repository.RoomRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class PeriodicGameChecker(
    private val roomRepository: RoomRepository,
    private val getRoomPeriodSeconds: Long,
    private val gameStateListener: GameStateListener) {
  private val disposables = CompositeDisposable()

  fun start(session: String) {
    disposables.add(Observable.interval(0, getRoomPeriodSeconds, TimeUnit.SECONDS)
        .flatMapSingle<Any> {
          roomRepository.getRoom(session)
              .observeOn(AndroidSchedulers.mainThread())
              .doOnSuccess { roomResponse: RoomResponse -> checkGameStatus(roomResponse) }
              .doOnError(Throwable::printStackTrace)
        }
        .subscribe())
  }

  private fun checkGameStatus(roomResponse: RoomResponse) {
    when (roomResponse.status) {
      RoomStatus.PLAYING -> gameStateListener.onUpdate(roomResponse)
      RoomStatus.COMPLETED -> gameStateListener.onFinishGame(roomResponse)
    }
  }

  fun stop() = disposables.clear()
}

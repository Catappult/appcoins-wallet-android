package cm.aptoide.skills.endgame


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import cm.aptoide.skills.usecase.GetRoomUseCase
import cm.aptoide.skills.usecase.SetFinalScoreUseCase
import com.appcoins.wallet.core.network.eskills.model.RoomResponse
import com.appcoins.wallet.core.network.eskills.model.RoomResult
import com.appcoins.wallet.core.network.eskills.model.RoomStatus
import com.appcoins.wallet.core.network.eskills.model.UserStatus
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// TODO !!!!!!!!
class FinishGameActivityViewModel @Inject constructor(
  getRoomUseCase: GetRoomUseCase,
  setFinalScore: SetFinalScoreUseCase,
  savedStateHandle: SavedStateHandle
) : ViewModel() {
  private val getRoomUseCase: GetRoomUseCase
  private val setFinalScore: SetFinalScoreUseCase
  private val session: String
  private val walletAddress: String
  private val userScore: Long

  init {
    this.getRoomUseCase = getRoomUseCase
    this.setFinalScore = setFinalScore
    this.session = savedStateHandle.get<String>("SESSION")!!
    this.walletAddress = savedStateHandle.get<String>("WALLET_ADDRESS")!!
    this.userScore = savedStateHandle.get<Long>("SCORE")!!
  }

  fun getRoom(): Single<RoomResponse> = getRoomUseCase.getRoom(session)

  fun getRoomResult(): Single<RoomResponse> =
    getRoomUseCase.getRoom(session).flatMap { roomResponse: RoomResponse ->
        if (roomResponse is RoomResponse.SuccessfulRoomResponse && roomResponse.currentUser.status !== UserStatus.PLAYING) {
          return@flatMap Single.just(roomResponse as RoomResponse)
        }
        setFinalScore.setFinalScore(session, userScore)
          .doOnError { throwable -> throwable.printStackTrace() }.onErrorReturnItem(roomResponse as RoomResponse.SuccessfulRoomResponse?)
      }.toObservable().repeatWhen { objectFlowable -> objectFlowable.delay(3, TimeUnit.SECONDS) }
      .skipWhile { roomResponse: RoomResponse -> isInProgress(roomResponse as? RoomResponse.SuccessfulRoomResponse) }.take(1)
      .singleOrError()

  fun isWinner(roomResult: RoomResult): Boolean {
    return roomResult.winner.walletAddress.equals(walletAddress, ignoreCase = true)
  }

  fun isTimeUp(roomResponse: RoomResponse.SuccessfulRoomResponse): Boolean {
    return roomResponse.currentUser.status == UserStatus.TIME_UP
  }

  private fun isInProgress(roomResponse: RoomResponse.SuccessfulRoomResponse?): Boolean {
    val completed = roomResponse?.status == RoomStatus.COMPLETED
    if (roomResponse != null) {
      for (user in roomResponse.users) {
        check(!(user.status === UserStatus.PLAYING && completed)) { "Match Completed but some players are still playing!" }
      }
    }
    return !completed
  }
}
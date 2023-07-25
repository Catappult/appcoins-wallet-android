package cm.aptoide.skills.endgame


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.usecase.GetRewardsPackagesUseCase
import cm.aptoide.skills.usecase.GetRoomUseCase
import cm.aptoide.skills.usecase.SetFinalScoreUseCase
import com.appcoins.wallet.core.network.eskills.model.RoomResponse
import com.appcoins.wallet.core.network.eskills.model.RoomResult
import com.appcoins.wallet.core.network.eskills.model.RoomStatus
import com.appcoins.wallet.core.network.eskills.model.UserStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SkillsEndgameViewModel @Inject constructor(
  private val walletAddressObtainer: WalletAddressObtainer,
  private val getRoomUseCase: GetRoomUseCase,
  private val setFinalScore: SetFinalScoreUseCase,
  private val getRewardsPackagesUseCase: GetRewardsPackagesUseCase,
  savedStateHandle: SavedStateHandle
) : ViewModel() {


  companion object {
    const val RESULT_OK = 0
    const val RESULT_RESTART = 1
    const val RESULT_SERVICE_UNAVAILABLE = 2
    const val RESULT_ERROR = 3
    const val RESULT_INVALID_URL = 4
  }

  private val session: String

  init {
    this.session = savedStateHandle["SESSION"]!!
  }

  fun getRoom(): Single<RoomResponse> = getRoomUseCase.getRoom(session)

  fun getRoomResult(): Single<RoomResponse> =
    getRoomUseCase.getRoom(session).flatMap { roomResponse: RoomResponse ->
      if (roomResponse is RoomResponse.SuccessfulRoomResponse) {
        if (roomResponse.currentUser.status !== UserStatus.PLAYING) {

          return@flatMap Single.just(roomResponse as RoomResponse)
        }
        setFinalScore.setFinalScore(session, roomResponse.currentUser.score)
          .doOnError { throwable -> throwable.printStackTrace() }
          .onErrorReturnItem(roomResponse as RoomResponse.SuccessfulRoomResponse?)
      } else
        Single.just(roomResponse)
    }.toObservable().repeatWhen { objectFlowable -> objectFlowable.delay(3, TimeUnit.SECONDS) }
      .skipWhile { roomResponse: RoomResponse -> isInProgress(roomResponse as? RoomResponse.SuccessfulRoomResponse) }
      .take(1)
      .singleOrError()

  fun getRewardsPackages(): Single<List<String>> = getRewardsPackagesUseCase.getRewardsPackages()

  fun isWinner(roomResult: RoomResult): Single<Boolean> {
    return walletAddressObtainer.getWalletAddress().map { walletAddress ->
      walletAddress.address.equals(roomResult.winner)
    }.subscribeOn(Schedulers.io())
  }

  fun getWalletAddress(): Single<String> =
    walletAddressObtainer.getWalletAddress().subscribeOn(Schedulers.io()).map { it.address }

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
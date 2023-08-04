package cm.aptoide.skills.usecase

import cm.aptoide.skills.repository.RoomRepository
import com.appcoins.wallet.core.network.eskills.model.RoomResponse
import com.appcoins.wallet.core.network.eskills.model.UserStatus
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SetFinalScoreUseCase @Inject constructor(roomRepository: RoomRepository) {
  private val roomRepository: RoomRepository

  init {
    this.roomRepository = roomRepository
  }

  fun setFinalScore(session: String, score: Long): Single<RoomResponse> {
    return roomRepository.patch(session, score, UserStatus.COMPLETED).subscribeOn(Schedulers.io())
      .retryWhen { throwableFlowable ->
        val counter = AtomicInteger()
        throwableFlowable.takeWhile { throwable ->
          (counter.getAndIncrement() != 3 && (throwable is SocketTimeoutException || throwable is HttpException))
        }.flatMap { Flowable.timer(counter.get().toLong(), TimeUnit.SECONDS) }
      }
  }
}
package cm.aptoide.skills

import androidx.fragment.app.Fragment
import cm.aptoide.skills.entity.UserData
import cm.aptoide.skills.model.TicketResponse
import cm.aptoide.skills.model.TicketStatus
import cm.aptoide.skills.usecase.*
import cm.aptoide.skills.util.EskillsUri
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


class SkillsViewModel(private val createTicketUseCase: CreateTicketUseCase,
                      private val payTicketUseCase: PayTicketUseCase,
                      private val getTicketUseCase: GetTicketUseCase,
                      private val getTicketRetryMillis: Long,
                      private val loginUseCase: LoginUseCase,
                      private val cancelTicketUseCase: CancelTicketUseCase,
                      private val closeView:PublishSubject<Pair<Int,UserData>>){
  lateinit var ticketId: String

  companion object {
    public const val RESULT_OK = 0
    public const val RESULT_USER_CANCELED = 1
    public const val RESULT_ERROR = 6

  }

  fun handleWalletCreationIfNeeded(): Observable<String> {
    return createTicketUseCase.getOrCreateWallet()
  }

  fun createTicket(eskillsUri: EskillsUri): Observable<TicketResponse> {
    return createTicketUseCase.createTicket(eskillsUri)
      .doOnSuccess { ticketId = it.ticketId }
      .toObservable()
  }

  fun getRoom(eskillsUri: EskillsUri, ticketResponse: TicketResponse,
              fragment: Fragment): Observable<UserData> {
    return payTicketUseCase.payTicket(ticketResponse.ticketId, ticketResponse.callbackUrl,
        ticketResponse.productToken, eskillsUri, fragment)
        .flatMap {
          val canProcceed = AtomicBoolean(false)

          getTicketUseCase.getTicket(ticketResponse.ticketId)
              .doOnSuccess { checkCanProcceed(it, canProcceed) }
              .delay(getTicketRetryMillis, TimeUnit.MILLISECONDS)
              .repeatUntil { canProcceed.get() }
              .skipWhile { !canProcceed.get() }
              .flatMapSingle { ticketResponse ->
                if (isRefunded(ticketResponse)) {
                  Single.just(UserData("", "", "", "", true))
                } else {
                  loginUseCase.login(ticketResponse.roomId!!)
                      .map { session ->
                        UserData(
                            ticketResponse.userId, ticketResponse.roomId,
                            ticketResponse.walletAddress, session
                        )
                      }
                }
              }
              .singleOrError()
        }
        .toObservable()
  }

  private fun isRefunded(ticketResponse: TicketResponse): Boolean {
    return ticketResponse.ticketStatus == TicketStatus.REFUNDED
  }

  private fun checkCanProcceed(ticketResponse: TicketResponse, canProcceed: AtomicBoolean) {
    if (ticketResponse.roomId != null || ticketResponse.ticketStatus == TicketStatus.REFUNDED) {
      canProcceed.set(true)
    }
  }

  fun getPayTicketRequestCode(): Int {
    return PayTicketUseCase.RC_ONE_STEP
  }

  fun payTicketOnActivityResult(resultCode: Int, txHash: String?) {

  }

  fun cancelTicket(): Single<TicketResponse> {
    // only paid tickets can be canceled/refunded on the backend side, meaning that if we
    // cancel before actually paying the backend will return a 409 HTTP. this way we allow
    // users to return to the game, without crashing, even if they weren't waiting in queue
    return cancelTicketUseCase.cancelTicket(ticketId)
      .doOnSuccess { closeView.onNext(Pair(RESULT_USER_CANCELED, UserData("", "", "", "", true)))  }
      .doOnError { closeView.onNext(Pair(RESULT_ERROR, UserData("", "", "", "", true))) }
  }

  fun closeView(): Observable<Pair<Int, UserData>> {
    return closeView
  }
}

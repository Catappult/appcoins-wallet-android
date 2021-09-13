package cm.aptoide.skills

import androidx.fragment.app.Fragment
import cm.aptoide.skills.entity.UserData
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.TicketResponse
import cm.aptoide.skills.model.TicketStatus
import cm.aptoide.skills.usecase.*
import cm.aptoide.skills.util.EskillsPaymentData
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit


class SkillsViewModel(
  private val walletAddressObtainer: WalletAddressObtainer,
  private val joinQueueUseCase: JoinQueueUseCase,
  private val navigator: SkillsNavigator,
  private val getTicketUseCase: GetTicketUseCase,
  private val getTicketRetryMillis: Long,
  private val loginUseCase: LoginUseCase,
  private val cancelTicketUseCase: CancelTicketUseCase,
  private val closeView:PublishSubject<Pair<Int,UserData>>){

  lateinit var ticketId: String

  companion object {
    const val RESULT_OK = 0
    const val RESULT_USER_CANCELED = 1
    const val RESULT_ERROR = 6

  }
  fun handleWalletCreationIfNeeded(): Observable<String> {
    return walletAddressObtainer.getOrCreateWallet()
  }

  fun joinQueue(eskillsPaymentData: EskillsPaymentData): Observable<TicketResponse> {
    return joinQueueUseCase.joinQueue(eskillsPaymentData)
      .doOnSuccess { ticketId = it.ticketId }
      .toObservable()
  }

  fun getRoom(
    eskillsPaymentData: EskillsPaymentData, ticketResponse: TicketResponse,
    fragment: Fragment
  ): Observable<UserData> {
    return Single.just(ticketResponse).flatMap {
      if (ticketResponse.ticketStatus == TicketStatus.IN_QUEUE) {
        Single.just(it)
      } else {
        navigator.navigateToPayTicket(
          ticketResponse.ticketId,
          ticketResponse.callbackUrl,
          ticketResponse.productToken,
          ticketResponse.ticketPrice,
          ticketResponse.priceCurrency,
          eskillsPaymentData,
          fragment
        )
      }
    }.flatMapObservable {
      getTicketUpdates(ticketResponse.ticketId)
        .flatMap { ticketResponse ->
          return@flatMap when (ticketResponse.ticketStatus) {
            TicketStatus.PENDING_PAYMENT -> Observable.just(
              UserData.fromStatus(UserData.Status.PAYING)
            )

            TicketStatus.REFUNDED -> Observable.just(
              UserData.fromStatus(UserData.Status.REFUNDED)
            )
            TicketStatus.IN_QUEUE, TicketStatus.REFUNDING -> Observable.just(
              UserData.fromStatus(UserData.Status.IN_QUEUE)
            )
            TicketStatus.COMPLETED -> loginUseCase.login(ticketResponse.roomId!!)
              .map { session ->
                return@map UserData(
                  ticketResponse.userId, ticketResponse.roomId,
                  ticketResponse.walletAddress, session, UserData.Status.COMPLETED
                )
              }.toObservable()
          }
        }
    }
  }


  private fun getTicketUpdates(ticketId: String): Observable<TicketResponse> {
    return Observable.interval(getTicketRetryMillis, TimeUnit.MILLISECONDS)
      .switchMapSingle { getTicketUseCase.getTicket(ticketId) }
  }

  fun getPayTicketRequestCode(): Int {
    return SkillsNavigator.RC_ONE_STEP
  }

  fun cancelTicket(): Single<TicketResponse> {
    // only paid tickets can be canceled/refunded on the backend side, meaning that if we
    // cancel before actually paying the backend will return a 409 HTTP. this way we allow
    // users to return to the game, without crashing, even if they weren't waiting in queue
    return cancelTicketUseCase.cancelTicket(ticketId)
      .doOnSuccess {
        closeView.onNext(
          Pair(
            RESULT_USER_CANCELED,
            UserData("", "", "", "", UserData.Status.REFUNDED)
          )
        )
      }
      .doOnError {
        closeView.onNext(
          Pair(
            RESULT_ERROR,
            UserData("", "", "", "", UserData.Status.REFUNDED)
          )
        )
      }
  }

  fun closeView(): Observable<Pair<Int, UserData>> {
    return closeView
  }
}

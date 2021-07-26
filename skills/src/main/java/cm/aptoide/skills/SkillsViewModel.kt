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
  private val createTicketUseCase: CreateTicketUseCase,
  private val navigator: SkillsNavigator,
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
    return walletAddressObtainer.getOrCreateWallet()
  }

  fun createTicket(eskillsPaymentData: EskillsPaymentData): Observable<TicketResponse> {
    return createTicketUseCase.createTicket(eskillsPaymentData)
      .doOnSuccess { ticketId = it.ticketId }
      .toObservable()
  }

  fun getRoom(
    eskillsPaymentData: EskillsPaymentData, ticketResponse: TicketResponse,
    fragment: Fragment
  ): Observable<UserData> {
    return navigator.navigateToPayTicket(
      ticketResponse.ticketId,
      ticketResponse.callbackUrl,
      ticketResponse.productToken,
      ticketResponse.ticketPrice,
      ticketResponse.priceCurrency,
      eskillsPaymentData,
      fragment
    )
      .flatMap {
        getTicketUpdates(ticketResponse.ticketId).filter { checkCanProceed(it) }
          .firstOrError()
          .flatMap { ticketResponse ->
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
      }
      .toObservable()
  }

  private fun getTicketUpdates(ticketId: String): Observable<TicketResponse> {
    return Observable.interval(getTicketRetryMillis, TimeUnit.MILLISECONDS)
      .switchMapSingle { getTicketUseCase.getTicket(ticketId) }
  }

  private fun isRefunded(ticketResponse: TicketResponse): Boolean {
    return ticketResponse.ticketStatus == TicketStatus.REFUNDED
  }

  private fun checkCanProceed(ticketResponse: TicketResponse): Boolean {
    return ticketResponse.roomId != null || ticketResponse.ticketStatus == TicketStatus.REFUNDED
  }

  fun getPayTicketRequestCode(): Int {
    return SkillsNavigator.RC_ONE_STEP
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

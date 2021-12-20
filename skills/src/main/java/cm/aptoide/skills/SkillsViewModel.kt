package cm.aptoide.skills

import cm.aptoide.skills.entity.UserData
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.*
import cm.aptoide.skills.usecase.*
import cm.aptoide.skills.util.EskillsPaymentData
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal
import java.util.concurrent.TimeUnit


class SkillsViewModel(
    private val walletAddressObtainer: WalletAddressObtainer,
    private val joinQueueUseCase: JoinQueueUseCase,
    private val getTicketUseCase: GetTicketUseCase,
    private val getTicketRetryMillis: Long,
    private val loginUseCase: LoginUseCase,
    private val cancelTicketUseCase: CancelTicketUseCase,
    private val closeView: PublishSubject<Pair<Int, UserData>>,
    private val payTicketUseCase: PayTicketUseCase,
    private val saveQueueIdToClipboard: SaveQueueIdToClipboard,
    private val getApplicationInfoUseCase: GetApplicationInfoUseCase,
    private val getTicketPriceUseCase: GetTicketPriceUseCase) {

  lateinit var ticketId: String

  companion object {
    const val RESULT_OK = 0
    const val RESULT_USER_CANCELED = 1
    const val RESULT_REGION_NOT_SUPPORTED = 2
    const val RESULT_SERVICE_UNAVAILABLE = 3
    const val RESULT_ERROR = 6
  }

  fun handleWalletCreationIfNeeded(): Observable<String> {
    return walletAddressObtainer.getOrCreateWallet()
  }

  fun joinQueue(eskillsPaymentData: EskillsPaymentData): Observable<Ticket> {
    return joinQueueUseCase.joinQueue(eskillsPaymentData)
        .doOnSuccess { if (it is CreatedTicket) ticketId = it.ticketId }
        .toObservable()
  }

  fun getRoom(
      eskillsPaymentData: EskillsPaymentData, ticket: CreatedTicket
  ): Observable<UserData> {
    return Single.just(ticket)
        .flatMap {
          if (ticket.processingStatus == ProcessingStatus.IN_QUEUE) {
            Single.just(it)
          } else {
            payTicketUseCase.pay(ticket, eskillsPaymentData)
          }
        }
        .flatMapObservable {
          getTicketUpdates(ticket.ticketId)
              .flatMap {
                return@flatMap handlePurchasedTicketStatus(it)
              }
        }
  }

  private fun handlePurchasedTicketStatus(ticket: Ticket): Observable<UserData> {
    return when (ticket) {
      is CreatedTicket -> {
        return when (ticket.processingStatus) {
          ProcessingStatus.PENDING_PAYMENT -> Observable.just(
              UserData.fromStatus(UserData.Status.PAYING)
          )
          ProcessingStatus.REFUNDED -> Observable.just(
              UserData.fromStatus(UserData.Status.REFUNDED)
          )
          ProcessingStatus.IN_QUEUE, ProcessingStatus.REFUNDING -> Observable.just(
              UserData.fromStatus(UserData.Status.IN_QUEUE, ticket.queueId)
          )
        }
      }
      is PurchasedTicket -> {
        loginUseCase.login(ticket.roomId, ticket.ticketId)
            .map { session ->
              return@map UserData(
                  ticket.userId, ticket.roomId, ticket.walletAddress, session,
                  UserData.Status.COMPLETED, ticket.queueId
              )
            }
            .toObservable()
      }
      is FailedTicket -> Observable.just(UserData.fromStatus(UserData.Status.FAILED))
    }
  }

  private fun getTicketUpdates(ticketId: String): Observable<Ticket> {
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
              Pair(RESULT_USER_CANCELED, UserData.fromStatus(UserData.Status.REFUNDED)))
        }
        .doOnError {
          closeView.onNext(
              Pair(RESULT_ERROR, UserData.fromStatus(UserData.Status.REFUNDED))
          )
        }
  }

  fun closeView(): Observable<Pair<Int, UserData>> {
    return closeView
  }

  fun saveQueueIdToClipboard(queueId: String) {
    saveQueueIdToClipboard.save(queueId)
  }

  fun getApplicationInfo(packageName: String): ApplicationInfo {
    return getApplicationInfoUseCase.getInfo(packageName)
  }

  fun getLocalFiatAmount(value: BigDecimal, currency: String): Single<Price> {
    return getTicketPriceUseCase.getLocalPrice(value, currency)
  }

  fun getFiatToAppcAmount(value: BigDecimal, currency: String): Single<String> {
    return getTicketPriceUseCase.getAppcPrice(value, currency)
  }
}

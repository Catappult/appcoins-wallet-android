package cm.aptoide.skills

import android.content.Context
import android.content.Intent
import cm.aptoide.skills.entity.UserData
import cm.aptoide.skills.interfaces.PaymentView
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.*
import cm.aptoide.skills.usecase.*
import cm.aptoide.skills.util.EskillsPaymentData
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
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
    private val saveQueueIdToClipboardUseCase: SaveQueueIdToClipboardUseCase,
    private val getApplicationInfoUseCase: GetApplicationInfoUseCase,
    private val getTicketPriceUseCase: GetTicketPriceUseCase,
    private val getUserBalanceUseCase: GetUserBalanceUseCase,
    private val sendUserToTopUpFlowUseCase: SendUserToTopUpFlowUseCase,
    private val hasAuthenticationPermissionUseCase: HasAuthenticationPermissionUseCase,
    private val getAuthenticationIntentUseCase: GetAuthenticationIntentUseCase,
    private val cachePaymentUseCase: CachePaymentUseCase,
    private val getCachedPaymentUseCase: GetCachedPaymentUseCase
) {
  lateinit var ticketId: String

  companion object {
    const val RESULT_OK = 0
    const val RESULT_USER_CANCELED = 1
    const val RESULT_REGION_NOT_SUPPORTED = 2
    const val RESULT_SERVICE_UNAVAILABLE = 3
    const val RESULT_ERROR = 6

    const val AUTHENTICATION_REQUEST_CODE = 33
  }

  fun handleWalletCreationIfNeeded(): Observable<String> {
    return walletAddressObtainer.getOrCreateWallet()
  }

  fun joinQueue(eskillsPaymentData: EskillsPaymentData): Observable<Ticket> {
    return joinQueueUseCase(eskillsPaymentData)
        .doOnSuccess { if (it is CreatedTicket) ticketId = it.ticketId }
        .toObservable()
  }

  fun getRoom(
      eskillsPaymentData: EskillsPaymentData, ticket: CreatedTicket, view: PaymentView
  ): Observable<UserData> {
    return Single.just(ticket)
        .flatMap {
          if (ticket.processingStatus == ProcessingStatus.IN_QUEUE) {
            Single.just(it)
          } else {
            Single.fromCallable {
              if (hasAuthenticationPermissionUseCase()) {
                cachePaymentUseCase(ticket, eskillsPaymentData)
                view.showFingerprintAuthentication()
              } else {
                payTicketUseCase(ticket, eskillsPaymentData)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { view.showLoading() }
                    .map { paymentResult -> handlePaymentResultStatus(view, paymentResult, ticket) }
              }
            }
          }
        }
        .flatMapObservable {
          getTicketUpdates(ticket.ticketId)
              .flatMap {
                return@flatMap handlePurchasedTicketStatus(it)
              }
        }
  }

  private fun handlePaymentResultStatus(view: PaymentView,
                                        paymentResult: PaymentResult,
                                        ticket: CreatedTicket): Single<Ticket> {
    when (paymentResult) {
      is SuccessfulPayment -> view.hideLoading()
      is FailedPayment.GenericError -> view.showError()
      is FailedPayment.FraudError -> view.showFraudError()
      is FailedPayment.NoNetworkError -> view.showNoNetworkError()
    }
    return Single.just(ticket)
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
        loginUseCase(ticket.roomId, ticket.ticketId)
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
        .switchMapSingle { getTicketUseCase(ticketId) }
  }

  fun cancelPayment() {
    closeView.onNext(
        Pair(RESULT_USER_CANCELED, UserData.fromStatus(UserData.Status.REFUNDED)))
  }

  fun cancelTicket(): Single<TicketResponse> {
    // only paid tickets can be canceled/refunded on the backend side, meaning that if we
    // cancel before actually paying the backend will return a 409 HTTP. this way we allow
    // users to return to the game, without crashing, even if they weren't waiting in queue
    return cancelTicketUseCase(ticketId)
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
    saveQueueIdToClipboardUseCase(queueId)
  }

  fun getApplicationInfo(packageName: String): ApplicationInfo {
    return getApplicationInfoUseCase(packageName)
  }

  fun getLocalFiatAmount(value: BigDecimal, currency: String): Single<Price> {
    return getTicketPriceUseCase.getLocalPrice(value, currency)
  }

  fun getFiatToAppcAmount(value: BigDecimal, currency: String): Single<Price> {
    return getTicketPriceUseCase.getAppcPrice(value, currency)
  }

  fun getFormattedAppcAmount(value: BigDecimal, currency: String): Single<String> {
    return getTicketPriceUseCase.getAppcFormatted(value, currency)
  }

  fun getCreditsBalance(): Single<BigDecimal> {
    return getUserBalanceUseCase()
  }

  fun sendUserToTopUpFlow(context: Context) {
    sendUserToTopUpFlowUseCase(context)
  }

  fun getAuthenticationIntent(context: Context): Intent {
    return getAuthenticationIntentUseCase(context)
  }

  fun restorePurchase(view: PaymentView): Single<Ticket> {
    return walletAddressObtainer.getWalletAddress()
        .flatMap { walletAddress ->
          getCachedPaymentUseCase(walletAddress).flatMap { cachedPayment ->
            payTicketUseCase(cachedPayment.ticket, cachedPayment.eskillsPaymentData)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { view.showLoading() }
                .flatMap { paymentResult ->
                  handlePaymentResultStatus(view, paymentResult, cachedPayment.ticket)
                }
          }
        }
  }
}

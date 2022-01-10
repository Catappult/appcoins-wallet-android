package cm.aptoide.skills

import android.net.Uri
import androidx.fragment.app.Fragment
import cm.aptoide.skills.entity.UserData
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.*
import cm.aptoide.skills.usecase.*
import cm.aptoide.skills.util.EskillsPaymentData
import cm.aptoide.skills.util.UriValidationResult
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern


class SkillsViewModel(
    private val walletAddressObtainer: WalletAddressObtainer,
    private val joinQueueUseCase: JoinQueueUseCase,
    private val navigator: SkillsNavigator,
    private val getTicketUseCase: GetTicketUseCase,
    private val getTicketRetryMillis: Long,
    private val loginUseCase: LoginUseCase,
    private val cancelTicketUseCase: CancelTicketUseCase,
    private val closeView: PublishSubject<Pair<Int, UserData>>) {

  lateinit var ticketId: String

  companion object {
    const val RESULT_OK = 0
    const val RESULT_USER_CANCELED = 1
    const val RESULT_REGION_NOT_SUPPORTED = 2
    const val RESULT_SERVICE_UNAVAILABLE = 3
    const val RESULT_ERROR = 6
    const val RESULT_INVALID_URL = 7
    const val RESULT_INVALID_USERNAME = 8
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
      eskillsPaymentData: EskillsPaymentData, ticket: CreatedTicket, fragment: Fragment
  ): Observable<UserData> {
    return Single.just(ticket)
        .flatMap {
          if (ticket.processingStatus == ProcessingStatus.IN_QUEUE) {
            Single.just(it)
          } else {
            navigator.navigateToPayTicket(ticket, eskillsPaymentData, fragment)
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
              UserData.fromStatus(UserData.Status.IN_QUEUE)
          )
        }
      }
      is PurchasedTicket -> {
        loginUseCase.login(ticket.roomId, ticket.ticketId)
            .map { session ->
              return@map UserData(
                  ticket.userId, ticket.roomId, ticket.walletAddress, session,
                  UserData.Status.COMPLETED
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

  fun validateUrl(uriString: String): UriValidationResult {
    val uri: Uri = Uri.parse(uriString)
    if (hasInvalidRequestStructure(uriString, uri)) {
      return UriValidationResult.Invalid(RESULT_INVALID_URL)
    }
    if (usernameContainsInvalidCharacters(uri)) {
      return UriValidationResult.Invalid(RESULT_INVALID_USERNAME)
    }
    return UriValidationResult.Valid(uri)
  }

  private fun usernameContainsInvalidCharacters(eskillsUri: Uri): Boolean {
    val pattern = Pattern.compile("[^A-Za-z0-9_ ]+")
    val username = eskillsUri.getQueryParameter("user_name")!!
    val matcher: Matcher = pattern.matcher(username)
    return matcher.find()

  }

  private fun hasInvalidRequestStructure(uriString: String, parsedUri: Uri): Boolean {
    val parametersString = uriString.split("?")[1]
    if (parametersString.contains("#")) {
      return true
    }
    val parametersArray = parametersString.split("&")
    return parsedUri.queryParameterNames.size != parametersArray.size
  }

  fun closeView(): Observable<Pair<Int, UserData>> {
    return closeView
  }
}

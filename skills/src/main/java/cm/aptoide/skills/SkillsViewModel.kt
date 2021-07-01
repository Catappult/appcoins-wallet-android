package cm.aptoide.skills

import androidx.fragment.app.Fragment
import cm.aptoide.skills.entity.UserData
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.TicketResponse
import cm.aptoide.skills.model.TicketStatus
import cm.aptoide.skills.usecase.*
import cm.aptoide.skills.util.EskillsUri
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit


class SkillsViewModel(private val walletAddressObtainer: WalletAddressObtainer,
                      private val createTicketUseCase: CreateTicketUseCase,
                      private val navigator: SkillsNavigator,
                      private val getTicketUseCase: GetTicketUseCase,
                      private val getTicketRetryMillis: Long,
                      private val loginUseCase: LoginUseCase,
                      private val cancelTicketUseCase: CancelTicketUseCase) {
  fun handleWalletCreationIfNeeded(): Observable<String> {
    return walletAddressObtainer.getOrCreateWallet()
  }

  fun createTicket(eskillsUri: EskillsUri): Observable<TicketResponse> {
    return createTicketUseCase.createTicket(eskillsUri)
        .toObservable()
  }

  fun getRoom(eskillsUri: EskillsUri, ticketResponse: TicketResponse,
              fragment: Fragment): Observable<UserData> {
    return navigator.navigateToPayTicket(
        ticketResponse.ticketId,
        ticketResponse.callbackUrl,
        ticketResponse.productToken,
        ticketResponse.ticketPrice,
        ticketResponse.priceCurrency,
        eskillsUri,
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
        .flatMapSingle { getTicketUseCase.getTicket(ticketId) }
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

  fun cancelTicket(ticketId: String): Single<TicketResponse> {
    return cancelTicketUseCase.cancelTicket(ticketId)
  }
}

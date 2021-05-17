package cm.aptoide.skills

import androidx.fragment.app.Fragment
import cm.aptoide.skills.entity.UserData
import cm.aptoide.skills.model.TicketResponse
import cm.aptoide.skills.model.TicketStatus
import cm.aptoide.skills.usecase.CreateTicketUseCase
import cm.aptoide.skills.usecase.GetTicketUseCase
import cm.aptoide.skills.usecase.LoginUseCase
import cm.aptoide.skills.usecase.PayTicketUseCase
import cm.aptoide.skills.util.EskillsUri
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


class SkillsViewModel(private val createTicketUseCase: CreateTicketUseCase,
                      private val payTicketUseCase: PayTicketUseCase,
                      private val getTicketUseCase: GetTicketUseCase,
                      private val getTicketRetryMillis: Long,
                      private val loginUseCase: LoginUseCase) {
  fun handleWalletCreationIfNeeded(): Observable<String> {
    return createTicketUseCase.getOrCreateWallet()
  }

  fun getRoom(eskillsUri: EskillsUri, fragment: Fragment): Observable<UserData> {
    return createTicketUseCase.createTicket(eskillsUri)
        .flatMap { ticketResponse ->
          payTicketUseCase.payTicket(ticketResponse.ticketId, ticketResponse.callbackUrl,
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
}

package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.repository.TransactionsRepository
import com.appcoins.wallet.core.analytics.analytics.logging.Log
import com.appcoins.wallet.core.network.backend.model.BackendTransactionType
import com.appcoins.wallet.core.network.backend.model.TransactionResponse

import io.reactivex.Single
import javax.inject.Inject

class VerifyUserTopUpUseCase @Inject constructor(
  private val transactionsRepository: TransactionsRepository,
  private val walletAddressObtainer: WalletAddressObtainer
) {
  operator fun invoke(): Single<Status> {
    return walletAddressObtainer.getWalletAddress()
      .flatMap { wallet ->
        transactionsRepository.getTransactionList(
          wallet.address, listOf(
            BackendTransactionType.WALLET_TOPUP, BackendTransactionType.WEB_TOPUP, BackendTransactionType.BONUS_GIFTCARD
          ), MAX_NR_OF_TRANSACTIONS, 0
        )
          .map { transactions ->
            when {
              transactions.isEmpty() -> Status.NO_TOPUP
              !hasValidTransaction(transactions) -> Status.PAYMENT_METHOD_NOT_SUPPORTED
              else -> Status.AVAILABLE
            }
          }
      }
      .doOnError {Log.d("VerifyTopUp", it.stackTraceToString())}
  }

  private fun hasValidTransaction(transactions: List<TransactionResponse>): Boolean {
    return transactions.any { transaction ->
      transaction.paymentMethod !in CODAPAY_PAYMENT_METHODS
    }
  }

  companion object {
    val CODAPAY_PAYMENT_METHODS = listOf(
      "alfamart",
      "bank_transfer",
      "boleto",
      "dana",
      "doku_wallet",
      "gcash",
      "gopay",
      "linkaja",
      "ovo",
      "oxxo",
      "paytm_upi",
      "paytm_wallet",
      "qiwi",
      "rabbit_line_pay",
      "true_money_wallet",
      "yoo_money",
    )
    const val MAX_NR_OF_TRANSACTIONS = 100
  }
}


enum class Status {
  PAYMENT_METHOD_NOT_SUPPORTED, NO_TOPUP, AVAILABLE
}
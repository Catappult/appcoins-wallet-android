package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.PendingTransaction
import io.reactivex.Single
import io.reactivex.SingleEmitter
import it.czerwinski.android.hilt.annotations.BoundTo
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.EthTransaction
import javax.inject.Inject

@BoundTo(supertype = EthereumService::class)
class Web3jService @Inject constructor(private val web3j: Web3jProvider) : EthereumService {

  private fun isPending(ethTransaction: EthTransaction): Boolean {
    val transaction = ethTransaction.transaction.orElse(null)
    return if (transaction == null) {
      throw TransactionNotFoundException()
    } else {
      transaction.blockNumberRaw == null
    }
  }

  private fun getTransaction(hash: String, web3jClient: Web3j): Single<PendingTransaction> {
    return Single.create { emitter: SingleEmitter<PendingTransaction> ->
      try {
        if (!emitter.isDisposed) {
          val ethTransaction = web3jClient.ethGetTransactionByHash(hash)
            .send()
          if (ethTransaction.hasError()) {
            emitter.onError(
              RuntimeException(
                ethTransaction.error
                  .message
              )
            )
          } else {
            emitter.onSuccess(PendingTransaction(hash, isPending(ethTransaction)))
          }
        }
      } catch (e: Exception) {
        if (!emitter.isDisposed) {
          emitter.onError(e)
        }
      }
    }
  }

  override fun getTransaction(hash: String): Single<PendingTransaction> {
    return Single.defer { getTransaction(hash, web3j.default) }
  }
}

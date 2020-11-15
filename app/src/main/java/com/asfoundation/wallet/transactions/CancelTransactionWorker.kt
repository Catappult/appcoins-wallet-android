package com.asfoundation.wallet.transactions

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.RxWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.ui.iab.payments.common.model.WalletAddresses
import com.asfoundation.wallet.util.guardLet
import io.reactivex.Single
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers

/**
 * Deferred worker responsible for canceling a transaction
 */
class CancelTransactionWorker(appContext: Context, workerParams: WorkerParameters,
                              private val remoteRepository: RemoteRepository,
                              private val walletService: WalletService,
                              private val partnerAddressService: AddressService,
                              private val logger: Logger) :
    RxWorker(appContext, workerParams) {

  companion object {
    private const val TAG = "CancelTransactionWorker"
    const val GATEWAY_KEY = "gateway"
    const val UID_KEY = "uid"
    const val DOMAIN = "domain"
  }

  override fun createWork(): Single<Result> {
    val (gateway, uid, domain) = guardLet(inputData.getString(GATEWAY_KEY),
        inputData.getString(UID_KEY), inputData.getString(DOMAIN)) {
      logger.log(
          TAG, "Couldn't cancel transaction: Missing parameters.")
      return Single.just(Result.failure())
    }
    return getAddresses(domain)
        .flatMap { walletAddresses ->
          remoteRepository.cancelPayment(gateway, uid, walletAddresses.address,
              walletAddresses.signedAddress)
        }
        .map { success ->
          if (success) {
            logger.log(TAG, "$uid transaction canceled successfully.")
            return@map Result.success()
          }
          logger.log(TAG, "$uid transaction cancellation failed.")
          return@map Result.failure()
        }
  }

  private fun getAddresses(packageName: String): Single<WalletAddresses> {
    return Single.zip(walletService.getAndSignCurrentWalletAddress()
        .subscribeOn(Schedulers.io()), partnerAddressService.getStoreAddressForPackage(packageName)
        .subscribeOn(Schedulers.io()),
        partnerAddressService.getOemAddressForPackage(packageName)
            .subscribeOn(Schedulers.io()), Function3 { addressModel, storeAddress, oemAddress ->
      return@Function3 WalletAddresses(addressModel.address, addressModel.signedAddress,
          storeAddress, oemAddress)
    })
  }
}

class CancelTransactionWorkerFactory(private val remoteRepository: RemoteRepository,
                                     private val walletService: WalletService,
                                     private val partnerAddressService: AddressService,
                                     private val logger: Logger) : WorkerFactory() {
  override fun createWorker(appContext: Context, workerClassName: String,
                            workerParameters: WorkerParameters): ListenableWorker? {
    return when (workerClassName) {
      CancelTransactionWorker::class.java.name -> CancelTransactionWorker(
          appContext,
          workerParameters, remoteRepository, walletService, partnerAddressService, logger)
      else -> null
    }
  }
}

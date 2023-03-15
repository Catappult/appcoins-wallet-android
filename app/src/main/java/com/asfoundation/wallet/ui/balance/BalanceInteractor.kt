package com.asfoundation.wallet.ui.balance

import com.appcoins.wallet.bdsbilling.WalletAddressModel
import com.appcoins.wallet.ui.arch.RxSchedulers
import com.asfoundation.wallet.service.AccountWalletService
import com.asfoundation.wallet.verification.repository.BrokerVerificationRepository
import com.asfoundation.wallet.verification.ui.credit_card.WalletVerificationInteractor
import com.asfoundation.wallet.verification.ui.credit_card.network.VerificationStatus
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BalanceInteractor @Inject constructor(
  private val accountWalletService: AccountWalletService,
  private val walletVerificationInteractor: WalletVerificationInteractor,
  private val brokerVerificationRepository: BrokerVerificationRepository,
  private val rxSchedulers: com.appcoins.wallet.ui.arch.RxSchedulers
) {

  companion object {
    val BIG_DECIMAL_MINUS_ONE = BigDecimal("-1")
  }

  fun getSignedCurrentWalletAddress(): Single<WalletAddressModel> =
    accountWalletService.getAndSignCurrentWalletAddress()

  fun observeCurrentWalletVerified(): Observable<BalanceVerificationModel> {
    return getSignedCurrentWalletAddress().flatMapObservable { addressModel ->
      Observable.mergeDelayError(
        Observable.just(getCachedVerificationStatus(addressModel.address))
          .map { verificationStatus ->
            mapToBalanceVerificationModel(addressModel.address, verificationStatus, null)
          }, observeWalletVerification(addressModel.address, addressModel.signedAddress)
      )
    }
  }

  fun observeWalletVerification(
    address: String,
    signedAddress: String
  ): Observable<BalanceVerificationModel> {
    return Observable.interval(0, 5, TimeUnit.SECONDS, rxSchedulers.io)
      .timeInterval()
      .flatMap {
        brokerVerificationRepository.getVerificationStatus(address, signedAddress)
          .toObservable()
          .map { status ->
            mapToBalanceVerificationModel(address, status, getCachedVerificationStatus(address))
          }
      }
      .takeUntil { verificationModel -> verificationModel.cachedStatus != BalanceVerificationStatus.VERIFYING }
  }

  private fun mapToBalanceVerificationModel(
    address: String,
    cachedVerificationStatus: VerificationStatus,
    verificationStatus: VerificationStatus?
  ): BalanceVerificationModel {
    return BalanceVerificationModel(
      address,
      mapToBalanceVerificationStatus(cachedVerificationStatus)!!,
      mapToBalanceVerificationStatus(verificationStatus)
    )
  }

  private fun mapToBalanceVerificationStatus(
    verificationStatus: VerificationStatus?
  ): BalanceVerificationStatus? {
    if (verificationStatus == null) return null
    return when (verificationStatus) {
      VerificationStatus.CODE_REQUESTED -> BalanceVerificationStatus.CODE_REQUESTED
      VerificationStatus.VERIFIED -> BalanceVerificationStatus.VERIFIED
      VerificationStatus.NO_NETWORK -> BalanceVerificationStatus.NO_NETWORK
      VerificationStatus.ERROR -> BalanceVerificationStatus.ERROR
      VerificationStatus.VERIFYING -> BalanceVerificationStatus.VERIFYING
      else -> BalanceVerificationStatus.UNVERIFIED
    }
  }


  fun getCachedVerificationStatus(address: String) =
    walletVerificationInteractor.getCachedVerificationStatus(address)
}

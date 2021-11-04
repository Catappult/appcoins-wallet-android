package com.asfoundation.wallet.ui.balance

import android.content.SharedPreferences
import android.util.Pair
import com.appcoins.wallet.bdsbilling.WalletAddressModel
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.repository.BackupRestorePreferencesRepository
import com.asfoundation.wallet.service.AccountWalletService
import com.asfoundation.wallet.ui.TokenValue
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.verification.repository.VerificationRepository
import com.asfoundation.wallet.verification.ui.credit_card.WalletVerificationInteractor
import com.asfoundation.wallet.verification.ui.credit_card.network.VerificationStatus
import io.reactivex.*
import io.reactivex.annotations.Nullable
import io.reactivex.functions.Function3
import java.math.BigDecimal

class BalanceInteractor(
    private val accountWalletService: AccountWalletService,
    private val balanceRepository: BalanceRepository,
    private val walletVerificationInteractor: WalletVerificationInteractor,
    private val backupRestorePreferencesRepository: BackupRestorePreferencesRepository,
    private val verificationRepository: VerificationRepository,
    private val networkScheduler: Scheduler) {

  companion object {
    const val APPC_CURRENCY = "APPC_CURRENCY"
    const val APPC_C_CURRENCY = "APPC_C_CURRENCY"
    const val ETH_CURRENCY = "ETH_CURRENCY"
    val BIG_DECIMAL_MINUS_ONE = BigDecimal("-1")
  }

  fun getAppcBalance(): Observable<Pair<Balance, FiatValue>> {
    return accountWalletService.find()
        .subscribeOn(networkScheduler)
        .flatMapObservable { balanceRepository.getAppcBalance(it.address) }
  }

  fun getEthBalance(): Observable<Pair<Balance, FiatValue>> {
    return accountWalletService.find()
        .subscribeOn(networkScheduler)
        .flatMapObservable { balanceRepository.getEthBalance(it.address) }
  }

  fun getCreditsBalance(): Observable<Pair<Balance, FiatValue>> {
    return accountWalletService.find()
        .subscribeOn(networkScheduler)
        .flatMapObservable { balanceRepository.getCreditsBalance(it.address) }
  }

  private fun getStoredAppcBalance(walletAddress: String?): Single<Pair<Balance, FiatValue>> {
    return (walletAddress?.let { Single.just(it) } ?: accountWalletService.find()
        .map { it.address })
        .subscribeOn(networkScheduler)
        .flatMap { balanceRepository.getStoredAppcBalance(it) }
  }

  private fun getStoredEthBalance(walletAddress: String?): Single<Pair<Balance, FiatValue>> {
    return (walletAddress?.let { Single.just(it) } ?: accountWalletService.find()
        .map { it.address })
        .subscribeOn(networkScheduler)
        .flatMap { balanceRepository.getStoredEthBalance(it) }
  }

  private fun getStoredCreditsBalance(walletAddress: String?): Single<Pair<Balance, FiatValue>> {
    return (walletAddress?.let { Single.just(it) } ?: accountWalletService.find()
        .map { it.address })
        .subscribeOn(networkScheduler)
        .flatMap { balanceRepository.getStoredCreditsBalance(it) }
  }

  fun requestTokenConversion(): Observable<BalanceScreenModel> {
    return Observable.zip(
        getCreditsBalance(),
        getAppcBalance(),
        getEthBalance(),
        Function3 { creditsBalance, appcBalance, ethBalance ->
          mapToBalanceScreenModel(creditsBalance, appcBalance, ethBalance)
        }
    )
  }

  fun getTotalBalance(address: String): Observable<FiatValue> {
    return Observable.zip(
        balanceRepository.getCreditsBalance(address),
        balanceRepository.getAppcBalance(address),
        balanceRepository.getEthBalance(address),
        Function3 { creditsBalance, appcBalance, ethBalance ->
          getOverallBalance(mapToBalance(creditsBalance, APPC_C_CURRENCY),
              mapToBalance(appcBalance, APPC_CURRENCY), mapToBalance(ethBalance, ETH_CURRENCY))
        })
  }

  fun requestActiveWalletAddress(): Single<String> {
    return accountWalletService.find()
        .subscribeOn(networkScheduler)
        .map { it.address }
  }

  fun getSignedCurrentWalletAddress(): Single<WalletAddressModel> =
      accountWalletService.getAndSignCurrentWalletAddress()

  fun getStoredOverallBalance(@Nullable walletAddress: String? = null): Single<FiatValue> {
    return Single.zip(
        getStoredAppcBalance(walletAddress),
        getStoredEthBalance(walletAddress),
        getStoredCreditsBalance(walletAddress),
        Function3 { creditsBalance, appcBalance, ethBalance ->
          mapOverallBalance(creditsBalance, appcBalance, ethBalance)
        }
    )
  }

  fun getStoredBalanceScreenModel(walletAddress: String): Single<BalanceScreenModel> {
    return Single.zip(
        getStoredAppcBalance(walletAddress),
        getStoredEthBalance(walletAddress),
        getStoredCreditsBalance(walletAddress),
        Function3 { appcBalance, ethBalance, creditsBalance ->
          mapToBalanceScreenModel(creditsBalance, appcBalance, ethBalance)
        }
    )
  }

  fun observeCurrentWalletVerified(): Observable<BalanceVerificationModel> {
    return getSignedCurrentWalletAddress()
        .flatMapObservable { addressModel ->
          Observable.merge(Observable.just(getCachedVerificationStatus(addressModel.address))
              .map { verificationStatus ->
                mapToBalanceVerificationModel(addressModel.address, verificationStatus, null)
              }, isWalletValid(addressModel.address, addressModel.signedAddress).toObservable())
        }
  }

  fun isWalletValid(address: String, signedAddress: String): Single<BalanceVerificationModel> {
    return verificationRepository.getVerificationStatus(address, signedAddress)
        .map { status ->
          mapToBalanceVerificationModel(address, status, getCachedVerificationStatus(address))
        }

  }

  private fun mapToBalanceVerificationModel(address: String,
                                            cachedVerificationStatus: VerificationStatus,
                                            verificationStatus: VerificationStatus?): BalanceVerificationModel {
    return BalanceVerificationModel(address,
        mapToBalanceVerificationStatus(cachedVerificationStatus)!!,
        mapToBalanceVerificationStatus(verificationStatus))
  }

  private fun mapToBalanceVerificationStatus(
      verificationStatus: VerificationStatus?): BalanceVerificationStatus? {
    if (verificationStatus == null) return null
    return when (verificationStatus) {
      VerificationStatus.CODE_REQUESTED -> BalanceVerificationStatus.CODE_REQUESTED
      VerificationStatus.VERIFIED -> BalanceVerificationStatus.VERIFIED
      VerificationStatus.NO_NETWORK -> BalanceVerificationStatus.NO_NETWORK
      VerificationStatus.ERROR -> BalanceVerificationStatus.ERROR
      else -> BalanceVerificationStatus.UNVERIFIED
    }
  }


  fun getCachedVerificationStatus(address: String) =
      walletVerificationInteractor.getCachedVerificationStatus(address)

  fun hasBackedUpOnce() = backupRestorePreferencesRepository.getBackedUpOnce()

  fun saveBackedUpOnce() = backupRestorePreferencesRepository.saveBackedUpOnce()

  fun observeBackedUpOnce(): Observable<Boolean> {
    return Observable.create(
        ObservableOnSubscribe { emitter: ObservableEmitter<Boolean> ->
          val listener =
              SharedPreferences.OnSharedPreferenceChangeListener { _, key: String ->
                if (key == BackupRestorePreferencesRepository.BACKED_UP_ONCE) {
                  emitter.onNext(backupRestorePreferencesRepository.getBackedUpOnce())
                }
              }
          emitter.setCancellable {
            backupRestorePreferencesRepository.removeChangeListener(listener)
          }
          emitter.onNext(backupRestorePreferencesRepository.getBackedUpOnce())
          backupRestorePreferencesRepository.addChangeListener(listener)
        } as ObservableOnSubscribe<Boolean>)
  }

  private fun mapOverallBalance(creditsBalance: Pair<Balance, FiatValue>,
                                appcBalance: Pair<Balance, FiatValue>,
                                ethBalance: Pair<Balance, FiatValue>): FiatValue {
    var balance = getAddBalanceValue(BIG_DECIMAL_MINUS_ONE,
        creditsBalance.second.amount)
    balance = getAddBalanceValue(balance, appcBalance.second.amount)
    balance = getAddBalanceValue(balance, ethBalance.second.amount)

    return FiatValue(balance, appcBalance.second.currency, appcBalance.second.symbol)

  }

  private fun mapToBalanceScreenModel(creditsBalance: Pair<Balance, FiatValue>,
                                      appcBalance: Pair<Balance, FiatValue>,
                                      ethBalance: Pair<Balance, FiatValue>): BalanceScreenModel {
    val credits = mapToBalance(creditsBalance, APPC_C_CURRENCY)
    val appc = mapToBalance(appcBalance, APPC_CURRENCY)
    val eth = mapToBalance(ethBalance, ETH_CURRENCY)
    val overall = getOverallBalance(credits, appc, eth)
    return BalanceScreenModel(overall, credits, appc, eth)
  }

  private fun mapToBalance(pair: Pair<Balance, FiatValue>, currency: String): TokenBalance {
    return TokenBalance(TokenValue(pair.first.value, currency, pair.first.symbol), pair.second)
  }

  private fun getOverallBalance(creditsBalance: TokenBalance,
                                appcBalance: TokenBalance,
                                ethBalance: TokenBalance): FiatValue {
    var balance = getAddBalanceValue(BIG_DECIMAL_MINUS_ONE,
        creditsBalance.fiat.amount)
    balance = getAddBalanceValue(balance, appcBalance.fiat.amount)
    balance = getAddBalanceValue(balance, ethBalance.fiat.amount)
    return FiatValue(balance, appcBalance.fiat.currency, appcBalance.fiat.symbol)
  }

  private fun getAddBalanceValue(currentValue: BigDecimal, value: BigDecimal): BigDecimal {
    return if (value.compareTo(BIG_DECIMAL_MINUS_ONE) == 1) {
      if (currentValue.compareTo(BIG_DECIMAL_MINUS_ONE) == 1) {
        currentValue.add(value)
      } else {
        value
      }
    } else {
      currentValue
    }
  }

  fun saveSeenBackupTooltip() {

  }

  fun hasSeenBackupTooltip(): Boolean {
    return true
  }

}

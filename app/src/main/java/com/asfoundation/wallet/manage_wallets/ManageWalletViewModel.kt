package com.asfoundation.wallet.manage_wallets

import androidx.lifecycle.ViewModel
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletInfoSimple
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatusCompound
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationType
import com.appcoins.wallet.feature.walletInfo.data.verification.WalletVerificationInteractor
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletsInteract
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletsModel
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.activeWalletAddress
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.inactiveWallets
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.ObserveWalletInfoUseCase
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.interact.DeleteWalletInteract
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ManageWalletViewModel
@Inject
constructor(
  private val displayChatUseCase: DisplayChatUseCase,
  private val observeWalletInfoUseCase: ObserveWalletInfoUseCase,
  private val walletsInteract: WalletsInteract,
  private val deleteWalletInteract: DeleteWalletInteract,
  private val walletService: WalletService,
  private val walletVerificationInteractor: WalletVerificationInteractor,
  private val analytics: ManageWalletAnalytics
) : ViewModel() {

  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  var uiState: StateFlow<UiState> = _uiState

  fun displayChat() {
    displayChatUseCase()
  }

  fun updateWallets() = getWallets()

  fun getWallets(walletChanged: Boolean = false) {
    walletsInteract
      .observeWalletsModel()
      .firstOrError()
      .doOnSubscribe { _uiState.value = UiState.Loading }
      .doOnSuccess { wallets ->
        getActiveWallet(wallets)
        if (walletChanged) _uiState.value = UiState.WalletChanged
      }
      .subscribe()
  }

  private fun getActiveWallet(wallets: WalletsModel) =
    observeWalletInfoUseCase(wallets.activeWalletAddress(), update = true)
      .firstOrError()
      .flatMapCompletable { walletInfo ->
        walletService.getAndSignCurrentWalletAddress()
          .flatMap { wallet ->
            Single.zip(
              walletVerificationInteractor.getVerificationStatus(
                address = wallet.address,
                type = VerificationType.PAYPAL
              ),
              walletVerificationInteractor.getVerificationStatus(
                address = wallet.address,
                type = VerificationType.CREDIT_CARD
              )
            ) { paypalStatus, creditCardStatus ->
              VerificationStatusCompound(
                creditCardStatus = creditCardStatus,
                payPalStatus = paypalStatus,
                currentVerificationType = walletVerificationInteractor
                  .getCurrentVerificationType(wallet.address)
              )
            }
          }
          .doOnSuccess { verificationStatus ->
            analytics.sendManageWalletScreenEvent()
            _uiState.value =
              UiState.Success(walletInfo, wallets.inactiveWallets(), verificationStatus)
          }
          .doOnError { error ->
            error.printStackTrace()
          }
          .ignoreElement()
      }
      .subscribeOn(Schedulers.io())
      .subscribe({}, { error ->
        error.printStackTrace()
      })

  fun deleteWallet(wallet: String) {
    deleteWalletInteract.delete(wallet)
      .doOnSubscribe { _uiState.value = UiState.Loading }
      .doOnComplete {
        _uiState.value = UiState.WalletDeleted
      }
      .doOnError { error ->
        error.printStackTrace()
      }
      .subscribe()
  }

  fun cancelVerification(walletAddress: String) {
    val cachedVerificationCC = walletVerificationInteractor.getCachedVerificationStatus(
      walletAddress,
      VerificationType.CREDIT_CARD
    )
    val cachedVerificationPP = walletVerificationInteractor.getCachedVerificationStatus(
      walletAddress,
      VerificationType.PAYPAL
    )
    if (
      cachedVerificationCC == VerificationStatus.CODE_REQUESTED ||
      cachedVerificationCC == VerificationStatus.VERIFYING
    ) {
      walletVerificationInteractor.removeWalletVerificationStatus(
        walletAddress,
        VerificationType.CREDIT_CARD
      )
        .subscribe()
    }
    if (
      cachedVerificationPP == VerificationStatus.CODE_REQUESTED ||
      cachedVerificationPP == VerificationStatus.VERIFYING
    ) {
      walletVerificationInteractor.removeWalletVerificationStatus(
        walletAddress,
        VerificationType.PAYPAL
      )
        .subscribe()
    }
  }

  sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    object WalletChanged : UiState()
    object WalletCreated : UiState()
    object WalletDeleted : UiState()
    data class Success(
      val activeWalletInfo: WalletInfo,
      val inactiveWallets: List<WalletInfoSimple>,
      val verificationStatus: VerificationStatusCompound
    ) : UiState()
  }
}

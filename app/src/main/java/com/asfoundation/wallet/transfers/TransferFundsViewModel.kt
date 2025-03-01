package com.asfoundation.wallet.transfers

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository.Status.API_ERROR
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository.Status.INVALID_AMOUNT
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository.Status.INVALID_WALLET_ADDRESS
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository.Status.NOT_ENOUGH_FUNDS
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository.Status.NO_INTERNET
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository.Status.SUCCESS
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository.Status.UNKNOWN_ERROR
import com.appcoins.wallet.core.utils.android_common.extensions.isNoNetworkException
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.ObserveWalletInfoUseCase
import com.asf.wallet.R
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.main.nav_bar.TransferNavigationItem
import com.asfoundation.wallet.ui.bottom_navigation.CurrencyDestinations
import com.asfoundation.wallet.ui.bottom_navigation.TransferDestinations
import com.asfoundation.wallet.ui.transact.TransferInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.Serializable
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class TransferFundsViewModel
@Inject
constructor(
  private val displayChatUseCase: DisplayChatUseCase,
  private val observeWalletInfoUseCase: ObserveWalletInfoUseCase,
  private val transferInteractor: TransferInteractor
) : ViewModel() {
  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  var uiState: StateFlow<UiState> = _uiState

  val clickedTransferItem: MutableState<Int?> = mutableStateOf(null)

  var currentAddedAddress: String = ""
  var currentAddedAmount: String = ""

  init {
    getWalletInfo()
  }

  fun getWalletInfo() {
    observeWalletInfoUseCase(null, update = true)
      .firstOrError()
      .doOnSuccess { _uiState.value = UiState.Success(it) }
      .subscribe()
  }

  fun displayChat() = displayChatUseCase()

  private fun handleError(throwable: Throwable) {
    if (throwable.isNoNetworkException()) {
      _uiState.value = UiState.NoNetworkError
    } else {
      _uiState.value = UiState.Error
    }
  }

  fun onClickSend(data: TransferData, packageName: String) {
    shouldBlockTransfer()
      .doOnSubscribe { _uiState.value = UiState.Loading }
      .flatMapCompletable { shouldBlock ->
        if (shouldBlock) {
          Completable.fromAction { _uiState.value = UiState.NavigateToWalletBlocked }
        } else {
          makeTransaction(data, packageName, null)
            .doOnSuccess { status ->
              handleTransferResult(data.currency, status, data.walletAddress, data.amount)
            }
            .doOnError { error ->
              handleError(error)
            }
            .flatMapCompletable {
              Completable.fromAction {}
            }
        }
      }
      .subscribe()
  }

  private fun handleTransferResult(
    currency: String,
    status: AppcoinsRewardsRepository.Status,
    walletAddress: String,
    amount: BigDecimal
  ) {
    when (status) {
      API_ERROR,
      UNKNOWN_ERROR,
      NO_INTERNET -> _uiState.value = UiState.UnknownError
      SUCCESS -> handleSuccess(currency, walletAddress, amount)
      INVALID_AMOUNT -> _uiState.value = UiState.InvalidAmountError
      INVALID_WALLET_ADDRESS -> _uiState.value = UiState.InvalidWalletAddressError
      NOT_ENOUGH_FUNDS -> _uiState.value = UiState.NotEnoughFundsError
    }
  }

  private fun handleSuccess(currency: String, walletAddress: String, amount: BigDecimal) {
    _uiState.value = UiState.SuccessAppcCreditsTransfer(walletAddress, amount, currency)
  }

  private fun makeTransaction(
    data: TransferData,
    packageName: String,
    guestWalletId: String?
  ): Single<AppcoinsRewardsRepository.Status> {
    return handleCreditsTransfer(
      walletAddress = data.walletAddress,
      amount = data.amount,
      currency = data.currency,
      packageName = packageName,
      guestWalletId = guestWalletId,
    )
  }

  private fun handleCreditsTransfer(
    walletAddress: String,
    amount: BigDecimal,
    currency: String,
    packageName: String,
    guestWalletId: String?
  ): Single<AppcoinsRewardsRepository.Status> {
    return Single.zip(
      Single.timer(1, TimeUnit.SECONDS),
      transferInteractor.transferCredits(
        toWallet = walletAddress,
        amount = amount,
        currency = currency,
        packageName = packageName,
        guestWalletId = guestWalletId
      )
    ) { _: Long,
        status: AppcoinsRewardsRepository.Status ->
      status
    }
  }

  private fun shouldBlockTransfer(): Single<Boolean> {
    return transferInteractor.isWalletBlocked()
  }

  fun transferNavigationItems() =
    listOf(
      TransferNavigationItem(
        destination = TransferDestinations.SEND,
        label = R.string.p2p_send_title,
        selected = true
      ),
      TransferNavigationItem(
        destination = TransferDestinations.RECEIVE,
        label = R.string.title_my_address,
        selected = false
      )
    )


  sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    object Error : UiState()
    object UnknownError : UiState()
    object InvalidAmountError : UiState()
    object InvalidWalletAddressError : UiState()
    object NotEnoughFundsError : UiState()
    object NoNetworkError : UiState()
    object NavigateToWalletBlocked : UiState()
    data class Success(val walletInfo: WalletInfo) : UiState()
    data class SuccessAppcCreditsTransfer(
      val walletAddress: String,
      val amount: BigDecimal,
      val currency: String
    ) : UiState()
  }

  data class TransferData(
    val walletAddress: String,
    val currency: String,
    val amount: BigDecimal
  ) : Serializable

}

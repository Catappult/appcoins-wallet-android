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
import com.asfoundation.wallet.main.nav_bar.CurrencyNavigationItem
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

  val clickedTransferItem: MutableState<Int> = mutableStateOf(TransferDestinations.SEND.ordinal)
  val clickedCurrencyItem: MutableState<Int> = mutableStateOf(CurrencyDestinations.APPC_C.ordinal)

  var currentAddedAddress: String = ""
  var currentAddedAmount: String = ""

  init {
    getWalletInfo()
  }

  private fun getWalletInfo() {
    observeWalletInfoUseCase(null, update = true, updateFiat = true)
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
    shouldBlockTransfer(data.currency)
      .doOnSubscribe { _uiState.value = UiState.Loading }
      .flatMapCompletable { shouldBlock ->
        if (shouldBlock) {
          Completable.fromAction { _uiState.value = UiState.NavigateToWalletBlocked }
        } else {
          makeTransaction(data, packageName)
            .doOnSuccess { status ->
              handleTransferResult(data.currency, status, data.walletAddress, data.amount)
            }
            .doOnError { error ->
              handleError(error)
            }
            .flatMapCompletable {
              Completable.fromAction{}
            }
        }
      }
      .subscribe()
  }

  private fun handleTransferResult(
    currency: Currency,
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

  private fun handleSuccess(currency: Currency, walletAddress: String, amount: BigDecimal) {
    when (currency) {
      Currency.APPC_C ->
        _uiState.value =
          UiState.SuccessAppcCreditsTransfer(walletAddress, amount, currency)

      Currency.APPC ->
        transferInteractor
          .find()
          .doOnSuccess {
            _uiState.value =
              UiState.NavigateToOpenAppcConfirmationView(it.address, walletAddress, amount)
          }
          .subscribe()

      Currency.ETH ->
        transferInteractor
          .find()
          .doOnSuccess {
            _uiState.value =
              UiState.NavigateToOpenEthConfirmationView(it.address, walletAddress, amount)
          }
          .subscribe()
    }
  }

  private fun makeTransaction(
    data: TransferData,
    packageName: String
  ): Single<AppcoinsRewardsRepository.Status> {
    return when (data.currency) {
      Currency.APPC_C -> handleCreditsTransfer(data.walletAddress, data.amount, packageName)
      Currency.ETH -> transferInteractor.validateEthTransferData(data.walletAddress, data.amount)
      Currency.APPC -> transferInteractor.validateAppcTransferData(data.walletAddress, data.amount)
    }
  }

  private fun handleCreditsTransfer(
    walletAddress: String,
    amount: BigDecimal,
    packageName: String
  ): Single<AppcoinsRewardsRepository.Status> {
    return Single.zip(
      Single.timer(1, TimeUnit.SECONDS),
      transferInteractor.transferCredits(walletAddress, amount, packageName)
    ) { _: Long,
        status: AppcoinsRewardsRepository.Status ->
      status
    }
  }

  private fun shouldBlockTransfer(currency: Currency): Single<Boolean> {
    return if (currency == Currency.APPC_C) {
      transferInteractor.isWalletBlocked()
    } else {
      Single.just(false)
    }
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

  fun currencyNavigationItems() =
    listOf(
      CurrencyNavigationItem(
        destination = CurrencyDestinations.APPC_C,
        label = R.string.p2p_send_currency_appc_c,
        selected = true
      ),
      CurrencyNavigationItem(
        destination = CurrencyDestinations.APPC,
        label = R.string.p2p_send_currency_appc,
        selected = false
      ),
      CurrencyNavigationItem(
        destination = CurrencyDestinations.ETHEREUM,
        label = R.string.p2p_send_currency_eth,
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
      val currency: Currency
    ) : UiState()

    data class NavigateToOpenAppcConfirmationView(
      val walletAddress: String,
      val toWalletAddress: String,
      val amount: BigDecimal
    ) : UiState()

    data class NavigateToOpenEthConfirmationView(
      val walletAddress: String,
      val toWalletAddress: String,
      val amount: BigDecimal
    ) : UiState()
  }

  data class TransferData(
    val walletAddress: String,
    val currency: Currency,
    val amount: BigDecimal
  ) : Serializable

  enum class Currency(val token: String) {
    APPC_C("APPC-C"),
    APPC("APPC"),
    ETH("ETH")
  }
}

package com.asfoundation.wallet.currency_setup_cg.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletGetterStatus
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import com.asfoundation.wallet.currency_setup_cg.viewModel.states.SetUpCurrencyVMState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SetUpCurrencyViewModel @Inject constructor(
  private val walletService: WalletService,
  private val rxSchedulers: RxSchedulers,
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  initialState: SetUpCurrencyVMState
) : ViewModel() {
  private val disposables = CompositeDisposable()
  private val _state = MutableStateFlow(initialState)
  val state = _state.asStateFlow()

  fun createWalletIfNeeded() {
    disposables
      .add(
        walletService.findWalletOrCreate()
          .subscribeOn(rxSchedulers.io)
          .observeOn(rxSchedulers.io)
          .doOnSubscribe {
            viewModelScope
              .launch {
                _state.emit(SetUpCurrencyVMState.Processing)
              }
          }
          .flatMap { walletAddress ->
            Observable.just(walletAddress)
              .filter { it != WalletGetterStatus.CREATING.toString() }
              .flatMapSingle {
                getWalletInfoUseCase(it, false)
              }
          }
          .subscribe({
            viewModelScope
              .launch {
                _state.emit(SetUpCurrencyVMState.Success)
              }
          }, {
            viewModelScope
              .launch {
                _state.emit(SetUpCurrencyVMState.Error)
              }
          })
      )
  }
}
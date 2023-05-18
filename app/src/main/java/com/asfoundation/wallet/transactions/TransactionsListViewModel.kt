package com.asfoundation.wallet.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.appcoins.wallet.core.utils.android_common.DateFormatterUtils.getDay
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asfoundation.wallet.change_currency.use_cases.GetSelectedCurrencyUseCase
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.home.usecases.FetchTransactionsHistoryPagingUseCase
import com.asfoundation.wallet.home.usecases.ObserveDefaultWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class TransactionsListViewModel
@Inject
constructor(
  private val fetchTransactionsHistoryUseCase: FetchTransactionsHistoryPagingUseCase,
  private val observeDefaultWalletUseCase: ObserveDefaultWalletUseCase,
  private val getSelectedCurrencyUseCase: GetSelectedCurrencyUseCase,
  private val displayChatUseCase: DisplayChatUseCase,
  private val logger: Logger
) : ViewModel() {
  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  var uiState: StateFlow<UiState> = _uiState

  companion object {
    private val TAG = TransactionsListViewModel::class.java.name
  }

  init {
    getWalletInfo()
  }

  fun displayChat() {
    displayChatUseCase()
  }

  private fun getWalletInfo() {
    Observable.combineLatest(
      getSelectedCurrencyUseCase(false).toObservable(), observeDefaultWalletUseCase()
    ) { selectedCurrency, wallet ->
      UiState.Success(WalletInfoModel(wallet.address, selectedCurrency))
    }
      .doOnSubscribe {
        _uiState.value = UiState.Loading
      }
      .doOnNext { newState ->
        _uiState.value = newState
      }
      .subscribe()
  }

  fun fetchTransactions(
    walletInfo: WalletInfoModel
  ): Flow<PagingData<UiModel>> {
    return Pager(
      config = PagingConfig(pageSize = 10),
      pagingSourceFactory = {
        fetchTransactionsHistoryUseCase.invoke(walletInfo.address, walletInfo.currency)
      }
    )
      .flow
      .catch { logger.log(TAG, it) }
      .map { pagingData -> pagingData.map { UiModel.TransactionItem(it) } }
      .map {
        it.insertSeparators { before, after ->
          if (after == null) return@insertSeparators null
          if (before == null) return@insertSeparators UiModel.SeparatorItem(after.transaction.date)
          if (before.transaction.date.getDay() != after.transaction.date.getDay())
            UiModel.SeparatorItem(after.transaction.date)
          else null
        }
      }
      .cachedIn(viewModelScope)
  }

  data class WalletInfoModel(val address: String, val currency: String)

  sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val walletInfo: WalletInfoModel) : UiState()
  }

  sealed class UiModel {
    data class TransactionItem(val transaction: TransactionModel) : UiModel()
    data class SeparatorItem(val date: String) : UiModel()
  }
}

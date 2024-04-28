package com.asfoundation.wallet.manage_cards

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.asfoundation.wallet.billing.adyen.PaymentBrands
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.manage_cards.models.StoredCard
import com.asfoundation.wallet.manage_cards.usecases.DeleteStoredCardUseCase
import com.asfoundation.wallet.manage_cards.usecases.GetStoredCardsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ManageCardsViewModel
@Inject
constructor(
  private val displayChatUseCase: DisplayChatUseCase,
  private val getStoredCardsUseCase: GetStoredCardsUseCase,
  private val deleteStoredCardUseCase: DeleteStoredCardUseCase
) : ViewModel() {

  sealed class UiState {
    object Loading : UiState()
    data class StoredCardsInfo(
      val storedCards: List<StoredCard>,
    ) : UiState()
  }

  private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
  var uiState: StateFlow<UiState> = _uiState

  val networkScheduler = Schedulers.io()
  val viewScheduler: Scheduler = AndroidSchedulers.mainThread()
  val showBottomSheet: MutableState<Boolean> = mutableStateOf(false)
  val storedCardClicked: MutableState<StoredCard?> = mutableStateOf(null)
  val isCardDeleted: MutableState<Boolean> = mutableStateOf(false)

  fun displayChat() {
    displayChatUseCase()
  }

  fun getCards() {
    getStoredCardsUseCase()
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .doOnSubscribe { _uiState.value = UiState.Loading }
      .doOnSuccess { cards ->
        _uiState.value = UiState.StoredCardsInfo(
          cards.map {
            StoredCard(
              cardLastNumbers = it.lastFour ?: "****",
              cardIcon = PaymentBrands.getPayment(it.brand).brandFlag,
              recurringReference = it.id
            )
          }
        )
      }
      .doOnError {

      }
      .subscribe()
  }

  fun showBottomSheet(show: Boolean = true, storedCard: StoredCard?) {
    storedCardClicked.value = storedCard
    showBottomSheet.value = show
  }

  fun deleteCard(recurringReference: String) {
    deleteStoredCardUseCase(recurringReference)
      .observeOn(viewScheduler)
      .doOnSubscribe { _uiState.value = UiState.Loading }
      .doOnSuccess {
        isCardDeleted.value = true
        showBottomSheet(false, null)
      }
      .doOnError {

      }
      .subscribe()
  }
}

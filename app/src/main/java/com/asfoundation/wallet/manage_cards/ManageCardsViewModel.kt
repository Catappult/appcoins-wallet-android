package com.asfoundation.wallet.manage_cards

import androidx.lifecycle.ViewModel
import com.asfoundation.wallet.billing.adyen.PaymentBrands
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.manage_cards.models.StoredCard
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
              cardIcon = PaymentBrands.getPayment(it.brand).brandFlag
            )
          }
        )
      }
      .subscribe()
  }

//  fun deleteCard(cardId: String) {
//    deleteCardInteract.delete(wallet)
//      .doOnSubscribe { _uiState.value = UiState.Loading }
//      .doOnComplete {
//        _uiState.value = UiState.CardDeleted
//      }
//      .subscribe()
//  }
}

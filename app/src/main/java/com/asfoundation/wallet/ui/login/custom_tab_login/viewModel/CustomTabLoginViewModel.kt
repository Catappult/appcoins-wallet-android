package com.asfoundation.wallet.ui.login.custom_tab_login.viewModel

import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.ui.login.custom_tab_login.CustomTabLoginActivity
import com.asfoundation.wallet.ui.login.custom_tab_login.viewModel.states.CustomTabVMStates
import com.asfoundation.wallet.ui.login.custom_tab_login.viewModel.states.CustomTabVMStates.FetchingUserKey
import com.asfoundation.wallet.ui.login.custom_tab_login.viewModel.states.CustomTabVMStates.FinishActivity
import com.asfoundation.wallet.ui.login.custom_tab_login.viewModel.states.CustomTabVMStates.FinishWithError
import com.asfoundation.wallet.ui.login.custom_tab_login.viewModel.states.CustomTabVMStates.Initial
import com.asfoundation.wallet.ui.login.webview_login.usecases.FetchUserKeyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for [CustomTabLoginActivity]
 *
 * Handles the login process via custom tabs and manages activity states.
 *
 * @property rxSchedulers Schedulers for managing threading in RxJava operations.
 * @property fetchUserKeyUseCase Use case for fetching the user key using an auth token
 * @property logger Logger for logging errors and information.
 * @param initialStates Initial state of the ViewModel, defaulting to [Initial].
 */
@HiltViewModel
internal class CustomTabLoginViewModel @Inject constructor(
  private val rxSchedulers: RxSchedulers,
  private val fetchUserKeyUseCase: FetchUserKeyUseCase,
  private val logger: Logger,
  initialStates: CustomTabVMStates = Initial,
) : androidx.lifecycle.ViewModel() {
  companion object {
    private const val TAG = "CustomTabLoginVM"
  }

  /**
   * MutableStateFlow to manage and observe the current state of the activity.
   */
  private val _activityState = MutableStateFlow(initialStates)

  /**
   * StateFlow exposing the current activity state to observers.
   */
  val activityState = _activityState.asStateFlow()

  /**
   * CompositeDisposable to manage RxJava subscriptions and prevent memory leaks.
   * Disposables are cleared when the ViewModel is cleared.
   * @see onCleared
   */
  private val disposables = CompositeDisposable()

  /**
   * Fetches the user key using the provided authentication token.
   *
   * Updates the activity state to [FetchingUserKey] while the operation is in progress.
   * On success, updates the state to [FinishActivity].
   * On failure, logs the error and updates the state to [FinishWithError].
   *
   * @param authToken The authentication token used to fetch the user key.
   * @see FetchUserKeyUseCase
   */
  fun fetchUserKey(authToken: String) {
    disposables
      .add(
        fetchUserKeyUseCase(authToken)
          .subscribeOn(rxSchedulers.io)
          .observeOn(rxSchedulers.io)
          .doOnSubscribe {
            viewModelScope.launch {
              _activityState.emit(FetchingUserKey)
            }
          }
          .subscribe({
            viewModelScope.launch {
              _activityState.emit(FinishActivity)
            }
          }, {
            it.printStackTrace()
            logger.log(TAG, "error in fetchUserKey: ${it.message}", it)
            viewModelScope.launch {
              _activityState.emit(FinishWithError)
            }
          })
      )
  }

  override fun onCleared() {
    disposables.clear()
    super.onCleared()
  }
}
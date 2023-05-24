package com.appcoins.wallet.core.arch

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.data.DataResult
import com.appcoins.wallet.core.arch.data.Error
import com.appcoins.wallet.core.utils.android_common.extensions.isNoNetworkException
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty1

abstract class NewBaseViewModel<S : ViewState>(initialState: S) : ViewModel() {

  private val _stateFlow: MutableStateFlow<S> = MutableStateFlow(initialState)
  val stateFlow: StateFlow<S> = _stateFlow
  val state: S get() = stateFlow.value

  /**
   * HashMap that holds repeatable jobs associated with an ID.
   * It exists to cancel previously running jobs when using [repeatableLaunchIn].
   */
  private val repeatableJobsMap = HashMap<String, Job>()

  /**
   * Handles a Flow to subscribe to it and set the state when a new value is emitted.
   *
   * @param started the subscription strategy to be used
   * @param initialValue the initial value to be set in the state
   */
  protected fun <T> Flow<T>.stateIn(
    started: SharingStarted = SharingStarted.WhileSubscribed(5_000),
    initialValue: T
  ): StateFlow<T> {
    return this.stateIn(scope = viewModelScope, started = started, initialValue = initialValue)
  }

  /**
   * Maps emissions from a Flow of values wrapped in Async and automatically emits
   * Async.Loading when called and Async.Fail on uncaught exception.
   *
   * Example usage:
   *   viewModelScope.launch {
   *     interactor.getLatestDataFlow()
   *         .mapAsyncToState(HomeState::dataAsync) { async -> copy(dataAsync = async) }
   *         .collect()
   *   }
   *
   * @param retainValue A state property that will be retained in case of Loading or Fail emissions
   * @param reducer A reducer that receives the latest emission from this flow as well as the
   *                current state and returns the new state to be set.
   *
   */
  protected suspend fun <T> Flow<T>.mapFlowToAsync(
    retainValue: KProperty1<S, Async<T>>? = null, reducer: S.(Async<T>) -> S
  ) {
    setState { reducer(Async.Loading(retainValue?.get(this)?.value)) }
    onEach { value -> setState { reducer(Async.Success(value)) } }
      .catch { e ->
        setState { reducer(Async.Fail(Error.UnknownError(e), retainValue?.get(this)?.value)) }
      }
      .collect()
  }

  /**
   * Maps emissions of Flow<DataResult<[T]>> to Async and subsequently updates the state
   * through [reducer]. If [retainValue] is set, the previous value of that state property
   * (if it exists) will be sent on loading and fail.
   *
   * Note that loading state is immediately set upon calling this.
   */
  protected fun <T> Flow<DataResult<T>>.mapFlowResultToAsync(
    retainValue: KProperty1<S, Async<T>>? = null,
    reducer: S.(Async<T>) -> S
  ): Flow<DataResult<T>> {
    setState { reducer(Async.Loading(retainValue?.get(this)?.value)) }
    return onEach { result ->
      result.toAsyncWithState(retainValue, reducer)
    }.catch { e ->
      setState { reducer(Async.Fail(Error.UnknownError(e), retainValue?.get(this)?.value)) }
    }
  }

  /**
   * Executes this suspend function and wraps the its state in Async. It automatically emits
   * Async.Loading on execute and Async.Fail on uncaught exception.
   *
   * Example usage:
   *   viewModelScope.launch {
   *     suspend { interactor.getData() }
   *         .mapAsyncToState(HomeState::dataAsync) { async -> copy(dataAsync = async) }
   *   }
   *
   * @param retainValue A state property that will be retained in case of Loading or Fail
   * @param reducer A reducer that receives the result of this suspend function as well as the
   *                current state and returns the new state to be set.
   */
  protected suspend fun <T : Any?> (suspend () -> T).mapSuspendToAsync(
    retainValue: KProperty1<S, Async<T>>? = null,
    reducer: S.(Async<T>) -> S
  ) {
    setState { reducer(Async.Loading(retainValue?.get(this)?.value)) }
    try {
      val result = this.invoke()
      setState { reducer(Async.Success(result)) }
    } catch (e: Exception) {
      setState { reducer(Async.Fail(Error.UnknownError(e), retainValue?.get(this)?.value)) }
    }
  }

  /**
   * Executes this suspend function and maps [DataResult] to [Async]. It automatically emits
   * Async.Loading on execute and Async.Fail on uncaught exception.
   */
  protected suspend fun <T> (suspend () -> DataResult<T>).mapResultToAsync(
    retainValue: KProperty1<S, Async<T>>? = null,
    reducer: S.(Async<T>) -> S
  ) {
    setState { reducer(Async.Loading(retainValue?.get(this)?.value)) }
    try {
      this.invoke().toAsyncWithState(retainValue, reducer)
    } catch (e: Exception) {
      setState { reducer(Async.Fail(Error.UnknownError(e), retainValue?.get(this)?.value)) }
    }
  }

  /**
   * Maps DataResult<T> to Async<T> and updates the state through [reducer]
   */
  private fun <T> DataResult<T>.toAsyncWithState(
    retainValue: KProperty1<S, Async<T>>? = null, reducer: S.(Async<T>) -> S
  ) {
    when (this) {
      is Ok -> setState { reducer(Async.Success(this@toAsyncWithState.value)) }
      is Err -> setState {
        reducer(Async.Fail(error, retainValue?.get(this)?.value))
      }
    }
  }

  protected fun setState(reducer: S.() -> S) {
    viewModelScope.launch {
      _stateFlow.update { state -> state.reducer() }
    }
  }

  private fun Throwable.mapToError(): Error {
    if (this.isNoNetworkException()) {
      return Error.ApiError.NetworkError(this)
    }
    return Error.UnknownError(this)
  }

  protected fun cancelSubscription(id: String) {
    repeatableJobsMap[id]?.cancel()
  }

  @CallSuper
  override fun onCleared() {
    repeatableJobsMap.clear()
  }

  protected fun withState(action: S.() -> Unit) = stateFlow.value.action()

  protected inline fun <A> subscribeState(
    prop: KProperty1<S, A>,
    crossinline actionBlock: suspend (A) -> Unit
  ) {
    stateFlow
      .map { s -> prop.get(s) }
      .distinctUntilChanged()
      .onEach { a -> actionBlock(a) }
      .launchIn(viewModelScope)
  }

  protected inline fun subscribeState(crossinline actionBlock: suspend (S) -> Unit) {
    stateFlow
      .onEach { s -> actionBlock(s) }
      .launchIn(viewModelScope)
  }

  /**
   * Launches a job for an [id]. If a job is already running with that [id], it cancels it first
   * before starting the new job.
   */
  protected fun <T> Flow<T>.repeatableLaunchIn(scope: CoroutineScope, id: String): Job {
    repeatableJobsMap[id]?.cancel()
    repeatableJobsMap[id] = scope.launch { collect() }
    return repeatableJobsMap[id]!!
  }
}
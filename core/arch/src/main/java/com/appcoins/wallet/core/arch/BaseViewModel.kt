package com.appcoins.wallet.core.arch

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.utils.android_common.extensions.isNoNetworkException
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.data.DataResult
import com.appcoins.wallet.core.arch.data.Error
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty1

interface ViewState
interface SideEffect

abstract class BaseViewModel<S : ViewState, E : SideEffect>(initialState: S) : ViewModel() {

  private val sideEffectsChannel = Channel<E>(Channel.BUFFERED)
  val sideEffectsFlow = sideEffectsChannel.receiveAsFlow()

  private val _stateFlow: MutableStateFlow<S> = MutableStateFlow(initialState)
  val stateFlow: StateFlow<S> = _stateFlow
  val state: S get() = stateFlow.value

  private val repeatableSubscriptionMap = HashMap<String, Disposable>()
  private val compositeDisposable = CompositeDisposable()

  protected fun sendSideEffect(eventBlock: S.() -> E?) {
    viewModelScope.launch {
      val sideEffect = stateFlow.value.eventBlock()
      sideEffect?.let { se -> sideEffectsChannel.send(se) }
    }
  }

  protected fun setState(reducer: S.() -> S) {
    viewModelScope.launch {
      _stateFlow.update { state -> state.reducer() }
    }
  }

  /**
   * Wraps the execution of an [Observable] in [Async] and reduces it to the global state through
   * [reducer]. It sends [Async.Loading] immediately once called and maps emissions to
   * [Async.Success]. If an exception is thrown, it sends [Async.Fail].
   *
   * @param retainValue A state property that will be retained for [Async.Loading] and [Async.Fail]
   *                    that is set when [Async.Loading] is emitted. It's useful if you want
   *                    to display previously successful data on loading or fail.
   * @param reducer A reducer that takes the current state and returns the new state. A common
   *                implementation of this with a data class is: `{ copy(stateProperty = it) }`.
   */
  protected fun <T> Observable<T>.asAsyncToState(
    retainValue: KProperty1<S, Async<T>>? = null,
    reducer: S.(Async<T>) -> S
  ): Observable<T> {
    setState { reducer(Async.Loading(retainValue?.get(this)?.value)) }
    return doOnNext { value ->
      setState {
        reducer(Async.Success(value, retainValue?.get(this)?.value))
      }
    }.doOnError { e ->
      setState {
        reducer(
          Async.Fail(
            e.mapToError(),
            retainValue?.get(this)?.value
          )
        )
      }
    }
  }

  /**
   * Wraps the execution of a [Single] in [Async] and reduces it to the global state through
   * [reducer]. It sends [Async.Loading] immediately once called and maps the emission to
   * [Async.Success]. If an exception is thrown, it sends [Async.Fail].
   *
   * @param retainValue A state property that will be retained for [Async.Loading] and [Async.Fail]
   *                    that is set when [Async.Loading] is emitted. It's useful if you want
   *                    to display previously successful data on loading or fail.
   * @param reducer A reducer that takes the current state and returns the new state. A common
   *                implementation of this with a data class is: `{ copy(stateProperty = it) }`.
   */
  protected fun <T> Single<T>.asAsyncToState(
    retainValue: KProperty1<S, Async<T>>? = null,
    reducer: S.(Async<T>) -> S
  ): Single<T> {
    setState { reducer(Async.Loading(retainValue?.get(this)?.value)) }
    return doOnSuccess { value ->
      setState {
        reducer(Async.Success(value, retainValue?.get(this)?.value))
      }
    }.doOnError { e ->
      setState {
        reducer(
          Async.Fail(
            e.mapToError(),
            retainValue?.get(this)?.value
          )
        )
      }
    }
  }

  /**
   * Wraps the execution of a [Completable] in [Async] and reduces it to the global state through
   * [reducer]. It sends [Async.Loading] immediately once called and maps to
   * [Async.Success] when completed. If an exception is thrown, it sends [Async.Fail].
   *
   * @param retainValue A state property that will be retained for [Async.Loading] and [Async.Fail]
   *                    that is set when [Async.Loading] is emitted. It's useful if you want
   *                    to display previously successful data on loading or fail.
   * @param reducer A reducer that takes the current state and returns the new state. A common
   *                implementation of this with a data class is: `{ copy(stateProperty = it) }`.
   */
  protected fun Completable.asAsyncToState(
    retainValue: KProperty1<S, Async<Unit>>? = null,
    reducer: S.(Async<Unit>) -> S
  ): Completable {
    setState { reducer(Async.Loading(retainValue?.get(this)?.value)) }
    return doOnComplete {
      setState {
        reducer(Async.Success(Unit, retainValue?.get(this)?.value))
      }
    }.doOnError { e ->
      setState {
        reducer(
          Async.Fail(
            e.mapToError(),
            retainValue?.get(this)?.value
          )
        )
      }
    }
  }

  /**
   * Wraps the execution of a [Single] in [Async] and reduces it to the global state through
   * [reducer]. It ONLY sends [Async.Loading] on subscription, while [asAsyncToState] also maps
   * success and failure.
   *
   * @param retainValue A state property that will be retained for [Async.Loading]
   *                    that is set when [Async.Loading] is emitted. It's useful if you want
   *                    to display previously successful data on loading.
   * @param reducer A reducer that takes the current state and returns the new state. A common
   *                implementation of this with a data class is: `{ copy(stateProperty = it) }`.
   */

  protected fun <T> Single<T>.asAsyncLoadingToState(
    retainValue: KProperty1<S, Async<T>>? = null,
    reducer: S.(Async<T>) -> S
  ): Single<T> {
    return doOnSubscribe {
      setState {
        reducer(
          Async.Loading(
            retainValue?.get(
              this
            )?.value
          )
        )
      }
    }
  }

  protected fun Completable.asAsyncLoadingToState(
    retainValue: KProperty1<S, Async<Unit>>? = null,
    reducer: S.(Async<Unit>) -> S
  ): Completable {
    return doOnSubscribe {
      setState {
        reducer(
          Async.Loading(
            retainValue?.get(
              this
            )?.value
          )
        )
      }
    }
  }

  private fun Throwable.mapToError(): Error {
    if (this.isNoNetworkException()) {
      return Error.ApiError.NetworkError(this)
    }
    return Error.UnknownError(this)
  }

  /**
   * Subscribes to the Observable source cancelling a previous subscription for the [id], if it
   * exists. The main purpose of this is to avoid concurrent subscriptions of the same observable
   * source (e.g. if we want to "refresh" an Observable). An alternative would be to use
   * a "refresh emitting" observable source and use switchMap, however that wouldn't work as nicely
   * with [asAsyncToState].
   *
   * Also subscribes safely to this view model scope. Once the ViewModel is cleared, this
   * subscription is also cleared.
   *
   * @param id Unique identifier for this subscription. Make sure you use different ids for different
   *           streams.
   * @param onErrorAction Action block that receives a [Throwable] and is executed at the end of
   *                      the stream if it isn't caught until then.
   */
  protected fun <T> Observable<T>.repeatableScopedSubscribe(
    id: String,
    onErrorAction: ((Throwable) -> Unit)? = null
  ): Disposable {
    repeatableSubscriptionMap[id]?.dispose()
    repeatableSubscriptionMap[id] = subscribe({}, { onErrorAction?.invoke(it) })
    return repeatableSubscriptionMap[id]!!.apply {
      compositeDisposable.add(this)
    }
  }

  /**
   * Subscribes to the Single source cancelling a previous subscription for the [id], if it
   * exists. The main purpose of this is to avoid concurrent subscriptions of the same Single
   * source (e.g. if we want to "refresh" a Single). An alternative would be to use
   * a "refresh emitting" observable source and use switchMap, however that wouldn't work as nicely
   * with [asAsyncToState].
   *
   * Also subscribes safely to this view model scope. Once the ViewModel is cleared, this
   * subscription is also cleared.
   *
   * @param id Unique identifier for this subscription. Make sure you use different ids for different
   *           streams.
   * @param onErrorAction Action block that receives a [Throwable] and is executed at the end of
   *                      the stream if it isn't caught until then.
   */
  protected fun <T> Single<T>.repeatableScopedSubscribe(
    id: String,
    onErrorAction: ((Throwable) -> Unit)? = null
  ): Disposable {
    repeatableSubscriptionMap[id]?.dispose()
    repeatableSubscriptionMap[id] = subscribe({}, { onErrorAction?.invoke(it) })
    return repeatableSubscriptionMap[id]!!.apply {
      compositeDisposable.add(this)
    }
  }

  /**
   * Subscribes to the Completable source cancelling a previous subscription for the [id], if it
   * exists. The main purpose of this is to avoid concurrent subscriptions of the same Completable
   * source (e.g. if we want to "refresh" a Completable). An alternative would be to use
   * a "refresh emitting" observable source and use switchMap, however that wouldn't work as nicely
   * with [asAsyncToState].
   *
   * Also subscribes safely to this view model scope. Once the ViewModel is cleared, this
   * subscription is also cleared.
   *
   * @param id Unique identifier for this subscription. Make sure you use different ids for different
   *           streams.
   * @param onErrorAction Action block that receives a [Throwable] and is executed at the end of
   *                      the stream if it isn't caught until then.
   */
  protected fun Completable.repeatableScopedSubscribe(
    id: String,
    onErrorAction: ((Throwable) -> Unit)? = null
  ): Disposable {
    repeatableSubscriptionMap[id]?.dispose()
    repeatableSubscriptionMap[id] = subscribe({}, { onErrorAction?.invoke(it) })
    return repeatableSubscriptionMap[id]!!.apply {
      compositeDisposable.add(this)
    }
  }

  protected fun cancelSubscription(id: String) {
    repeatableSubscriptionMap[id]?.dispose()
  }

  /**
   * Subscribes safely to this view model scope. Once the ViewModel is cleared, this subscription
   * is also cleared.
   */
  protected fun <T> Observable<T>.scopedSubscribe(
    onErrorAction: ((Throwable) -> Unit)? = null
  ): Disposable {
    val disposable = subscribe({}, { onErrorAction?.invoke(it) })
    return disposable.apply {
      compositeDisposable.add(this)
    }
  }

  /**
   * Subscribes safely to this view model scope. Once the ViewModel is cleared, this subscription
   * is also cleared.
   */
  protected fun <T> Single<T>.scopedSubscribe(
    onErrorAction: ((Throwable) -> Unit)? = null
  ): Disposable {
    val disposable = subscribe({}, { onErrorAction?.invoke(it) })
    return disposable.apply {
      compositeDisposable.add(this)
    }
  }

  /**
   * Subscribes safely to this view model scope. Once the ViewModel is cleared, this subscription
   * is also cleared.
   */
  protected fun Completable.scopedSubscribe(
    onErrorAction: ((Throwable) -> Unit)? = null
  ): Disposable {
    val disposable = subscribe({}, { onErrorAction?.invoke(it) })
    return disposable.apply {
      compositeDisposable.add(this)
    }
  }

  @CallSuper
  override fun onCleared() {
    repeatableSubscriptionMap.clear()
    compositeDisposable.clear()
    repeatableJobsMap.clear()
  }

  /**
   * HashMap that holds repeatable jobs associated with an ID.
   * It exists to cancel previously running jobs when using [repeatableLaunchIn].
   */
  private val repeatableJobsMap = HashMap<String, Job>()

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
  protected suspend fun <T> Flow<T>.flowMapAsyncToState(
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
  protected fun <T> Flow<DataResult<T>>.flowMapResultAsyncToState(
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
  protected suspend fun <T : Any?> (suspend () -> T).mapAsyncToState(
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
  protected suspend fun <T> (suspend () -> DataResult<T>).mapResultAsyncToState(
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
}
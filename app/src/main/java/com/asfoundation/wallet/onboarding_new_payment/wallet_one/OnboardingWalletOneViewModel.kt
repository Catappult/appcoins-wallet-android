package com.asfoundation.wallet.onboarding_new_payment.wallet_one

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.network.microservices.model.WalletOneTransaction
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.android_common.toSingleEvent
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.googlepay.usecases.WaitForSuccessUseCase
import com.asfoundation.wallet.billing.wallet_one.WalletOneReturnSchemas
import com.asfoundation.wallet.billing.wallet_one.models.WalletOneConst
import com.asfoundation.wallet.billing.wallet_one.usecases.*
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents
import com.asfoundation.wallet.onboarding_new_payment.wallet_one.OnboardingWalletOneFragmentArgs
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsView
import com.wallet.appcoins.feature.support.data.SupportInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class OnboardingWalletOneViewModel @Inject constructor(
  private val createWalletOneTransactionUseCase: CreateWalletOneTransactionUseCase,
  private val waitForSuccessWalletOneUseCase: WaitForSuccessUseCase,
  private val supportInteractor: SupportInteractor,
  private val inAppPurchaseInteractor: InAppPurchaseInteractor,
  private val rxSchedulers: RxSchedulers,
  private val events: OnboardingPaymentEvents,
  savedStateHandle: SavedStateHandle,
) : ViewModel() {

  sealed class State {
    object Start : State()
    data class Error(val stringRes: Int) : State()
    data class WebAuthentication(val htmlData: String) : State()
    object SuccessPurchase : State()
    object WalletOneBack : State()
    data class BackToGame(val domain: String) : State()
    object ExploreWallet : State()
  }

  val _state = MutableLiveData<State>(State.Start)
  val state = _state.toSingleEvent()

  private var compositeDisposable: CompositeDisposable = CompositeDisposable()

  private var args: OnboardingWalletOneFragmentArgs =
    OnboardingWalletOneFragmentArgs.fromSavedStateHandle(savedStateHandle)

  val networkScheduler = rxSchedulers.io
  val viewScheduler = rxSchedulers.main

  var uid: String? = null
  var shouldStartPayment = true

  fun startPayment(
    amount: BigDecimal,
    currency: String,
    transactionBuilder: TransactionBuilder,
    origin: String?
  ) {
    if (shouldStartPayment) {
      shouldStartPayment = false
      events.sendPaymentConfirmationWalletOneEvent(transactionBuilder)
      compositeDisposable.add(
        createWalletOneTransactionUseCase(
          value = (amount.toString()),
          currency = currency,
          transactionBuilder = transactionBuilder,
          origin = origin,
          method = PaymentType.WALLET_ONE.subTypes[0],
          successUrl = WalletOneReturnSchemas.SUCCESS.schema,
          failUrl = WalletOneReturnSchemas.ERROR.schema,
        )
          .subscribeOn(networkScheduler)
          .observeOn(viewScheduler)
          .map { transaction ->
            if (
              transaction?.validity == null ||
              transaction?.validity == WalletOneTransaction.WalletOneValidityState.ERROR
            ) {
              Log.d(TAG, "WalletOne transaction error. Error starting transaction")
              events.sendPaymentErrorMessageEvent(
                errorMessage = "WalletOne transaction error. Error starting transaction",
                transactionBuilder = transactionBuilder,
                paymentMethod = BillingAnalytics.PAYMENT_METHOD_WALLET_ONE,
              )
              _state.postValue(State.Error(R.string.purchase_error_one_wallet_generic))
            } else {
              uid = transaction.uid
              Log.d("htmlData", transaction.htmlData ?: "null")
              _state.postValue(State.WebAuthentication(transaction.htmlData ?: ""))
            }
          }.subscribe({}, {
            Log.d(TAG, it.toString())
            events.sendPaymentErrorMessageEvent(
              errorMessage = "WalletOne transaction error. Error starting transaction",
              transactionBuilder = transactionBuilder,
              paymentMethod = BillingAnalytics.PAYMENT_METHOD_WALLET_ONE,
            )
            _state.postValue(State.Error(R.string.purchase_error_one_wallet_generic))
          })
      )
    }
  }

  fun waitForSuccess(
    uid: String?,
    transactionBuilder: TransactionBuilder,
    wasNonSuccess: Boolean = false,
  ) {
    val disposableSuccessCheck = waitForSuccessWalletOneUseCase(uid ?: "")
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .subscribe({
        when (it.status) {
          PaymentModel.Status.COMPLETED -> {
            handleSuccess(it.uid, transactionBuilder)
          }
          PaymentModel.Status.FAILED, PaymentModel.Status.FRAUD, PaymentModel.Status.CANCELED, PaymentModel.Status.INVALID_TRANSACTION -> {
            Log.d(TAG, "Error on transaction on Settled transaction polling")
            events.sendPaymentErrorMessageEvent(
              errorMessage = "Error on transaction on Settled transaction polling ${it.status.name}",
              transactionBuilder = transactionBuilder,
              paymentMethod = BillingAnalytics.PAYMENT_METHOD_WALLET_ONE,
            )
            _state.postValue(State.Error(R.string.unknown_error))
          }
          else -> { /* pending */ }
        }
      }, {
        Log.d(TAG, "Error on Settled transaction polling")
        events.sendPaymentErrorMessageEvent(
          errorMessage = "Error on Settled transaction polling",
          transactionBuilder = transactionBuilder,
          paymentMethod = BillingAnalytics.PAYMENT_METHOD_WALLET_ONE,
        )
      })
    // disposes the check after x seconds
    viewModelScope.launch {
      delay(WalletOneConst.WALLET_ONE_TIMEOUT)
      try {
        if (state.value !is State.SuccessPurchase && wasNonSuccess)
          _state.postValue(State.Error(R.string.purchase_error_one_wallet_generic))
        disposableSuccessCheck.dispose()
      } catch (_: Exception) {
      }
    }
  }

  fun handleSuccess(
    purchaseUid: String?,
    transactionBuilder: TransactionBuilder
  ) {
    inAppPurchaseInteractor.savePreSelectedPaymentMethod(
      PaymentMethodsView.PaymentMethodId.WALLET_ONE.id
    )
    events.sendWalletOneSuccessFinishEvents(
      transactionBuilder = transactionBuilder,
      purchaseUid ?: ""
    )
    _state.postValue(State.SuccessPurchase)
  }


  fun showSupport() {
    compositeDisposable.add(
      supportInteractor.showSupport(0).subscribe({}, { it.printStackTrace() })
    )
  }


  fun handleBackToGameClick() {
    events.sendPaymentConclusionNavigationEvent(OnboardingPaymentEvents.BACK_TO_THE_GAME)
    _state.postValue(State.BackToGame(args.transactionBuilder.domain))
  }

  fun handleExploreWalletClick() {
    events.sendPaymentConclusionNavigationEvent(OnboardingPaymentEvents.EXPLORE_WALLET)
    _state.postValue(State.ExploreWallet)
  }

  companion object {
    private val TAG = OnboardingWalletOneViewModel::class.java.simpleName
  }

}
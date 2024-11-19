package com.asfoundation.wallet.iab.presentation.payment_methods.credit_card

import android.net.Uri
import androidx.activity.ComponentActivity
import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.components.base.BaseActionComponent
import com.adyen.checkout.redirect.RedirectComponent
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.asfoundation.wallet.iab.payment_manager.payment_methods.CreditCardPaymentMethod
import com.asfoundation.wallet.iab.presentation.PurchaseInfoData

sealed class CreditCardUiState {
  data object Loading : CreditCardUiState()
  data class Idle(
    val creditCardPaymentMethod: CreditCardPaymentMethod,
    val purchaseInfoData: PurchaseInfoData,
    val cardComponent: ((ComponentActivity) -> CardComponent?)?,
    val savingCreditCard: Boolean,
    val paymentInfoModel: PaymentInfoModel,
  ) : CreditCardUiState()

  data object CreditCardError : CreditCardUiState()
  data class VerifyScreen(val message: String) : CreditCardUiState()
  data class AddCreditCardError(
    val errorMessage: String,
  ) : CreditCardUiState()

  data object NoConnection : CreditCardUiState()
  data object Finish : CreditCardUiState()
  data class Handle3DSAction(
    val component: Adyen3DS2Component,
    val paymentModel: PaymentModel,
  ) : CreditCardUiState()

  data class HandleRedirectAction(
    val component: RedirectComponent,
    val uri: Uri,
    val paymentModel: PaymentModel,
  ) : CreditCardUiState()

  data class UserAction<C : BaseActionComponent<*>>(
    val component: C,
    val paymentModel: PaymentModel,
  ) : CreditCardUiState()
}
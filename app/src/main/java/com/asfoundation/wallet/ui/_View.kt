package com.asfoundation.wallet.ui

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import com.adyen.checkout.components.model.payments.response.Action
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.asfoundation.wallet.billing.adyen.AdyenCardWrapper
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.payments.carrier.verify.CarrierVerifyData
import java.math.BigDecimal
import java.util.*

interface _ViewState
object _CreatingWalletViewState : _ViewState
object _WalletCreatedViewState : _ViewState
data class _ErrorViewState(val message: Int? = null) : _ViewState
object _AuthenticationSuccessViewState : _ViewState
object _AuthenticationFailViewState : _ViewState
object _PaymentMethodsViewState : _ViewState
object _UpdateRequiredViewState : _ViewState
data class _WebViewResult(val data: Uri?) : _ViewState
object _NoFundsErrorViewState : _ViewState
object _WrongNetworkErrorViewState : _ViewState
object _NoTokenFundsErrorViewState : _ViewState
object _NoEtherFundsErrorViewState : _ViewState
object _NoNetworkErrorViewState : _ViewState
object _NonceErrorViewState : _ViewState
object _ApprovingViewState : _ViewState
object _BuyingViewState : _ViewState
object _TransactionCompletedViewState : _ViewState
data class _RaidenChannelValuesViewState(val data: List<BigDecimal>?) : _ViewState
object _ForbiddenErrorViewState : _ViewState
data class _VerificationViewState(val verified: Boolean = false) : _ViewState
object _LoadingViewState : _ViewState
object _LoadedViewState : _ViewState
data class _SetProductPriceViewState(val amount: String, val currency: String) : _ViewState
data class _SetCardDataViewState(
  val paymentInfoModel: PaymentInfoModel,
  val forget: Boolean,
  val onAdyenCardWrapper: ((adyenCardWrapper: AdyenCardWrapper) -> Unit)? = null
) :
  _ViewState

object _NetworkErrorViewState : _ViewState
object _CvvErrorViewState : _ViewState
data class _ErrorCodeViewState(val errorCode: Int = 0) : _ViewState
object _InvalidCardErrorViewState : _ViewState
object _SecurityValidationErrorViewState : _ViewState
object _OutdatedCardErrorViewState : _ViewState
object _AlreadyProcessedErrorViewState : _ViewState
object _PaymentErrorViewState : _ViewState
data class _BillingAddressViewState(val amount: BigDecimal, val currency: String) : _ViewState
data class _SuccessViewState(val date: Date?) : _ViewState
data class _VerificationErrorViewState(val verified: Boolean) : _ViewState
data class _SubmitUriResultViewState(val uri: Uri) : _ViewState
data class _Handle3DSActionViewState(val action: Action) : _ViewState
data class _FilterCountriesViewState(val countryList: String, val defaultCountry: String?) : _ViewState
object _PhoneNumberLayoutViewState : _ViewState
data class _CarrierVerifyDataViewState(val carrierVerifyData: CarrierVerifyData) : _ViewState
data class _AppDetailsViewState(val appName: String, val icon: Drawable) : _ViewState
object _InvalidPhoneNumberViewState : _ViewState

interface _Navigation
data class _StartTransfer(
  val transactionBuilder: TransactionBuilder,
  val isBds: Boolean
) :
  _Navigation

data class _StartSend(val data: String, val callerPackage: String?, val productName: String) :
  _Navigation

data class _StartESkills(val data: String, val callerPackage: String?, val productName: String) :
  _Navigation

data class _FinishActivity(val bundle: Bundle?) : _Navigation

data class _Finish(val bundle: Bundle?) : _Navigation

data class _PerkBonusAndGamification(val address: String) : _Navigation

data class _BackupNotification(val address: String) : _Navigation

data class _Close(val bundle: Bundle?) : _Navigation

data class _GoToUriForResult(val redirectUrl: String) : _Navigation

data class _GoToCarrierFee(
  val uid: String,
  val domain: String,
  val transactionData: String,
  val transactionType: String,
  val paymentUrl: String,
  val currency: String,
  val amount: BigDecimal,
  val appcAmount: BigDecimal,
  val bonus: BigDecimal?,
  val skuDescription: String,
  val skuId: String?,
  val feeFiatAmount: BigDecimal,
  val carrierName: String,
  val carrierImage: String,
  val phoneNumber: String
) : _Navigation

data class _GoToError(@StringRes val error: Int, val arg: String? = null) : _Navigation

object _FinishWithError : _Navigation

object _Back : _Navigation

interface _View {
  fun setState(state: _ViewState)
}

interface _Navigator {
  fun navigate(navigation: _Navigation)
}

class _MissingWalletException : RuntimeException()

class _MissingAmountException : RuntimeException()


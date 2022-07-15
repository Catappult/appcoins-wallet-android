package com.asfoundation.wallet.ui

import android.net.Uri
import android.os.Bundle
import com.asfoundation.wallet.entity.TransactionBuilder
import java.math.BigDecimal

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
object _VerificationViewState : _ViewState

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

interface _View {
  fun setState(state: _ViewState)
}

interface _Navigator {
  fun navigate(navigation: _Navigation)
}

class _MissingWalletException : RuntimeException()

class _MissingAmountException : RuntimeException()


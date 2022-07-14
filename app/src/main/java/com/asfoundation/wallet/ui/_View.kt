package com.asfoundation.wallet.ui

import com.asfoundation.wallet.entity.TransactionBuilder

interface _ViewState
object _CreatingWalletViewState : _ViewState
object _WalletCreatedViewState : _ViewState
data class _ErrorViewState(val message: Int? = null) : _ViewState

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

object _Close : _Navigation

interface _View {
  fun setState(state: _ViewState)
}

interface _Navigator {
  fun navigate(navigation: _Navigation)
}

class _MissingWalletException : RuntimeException()

class _MissingAmountException : RuntimeException()


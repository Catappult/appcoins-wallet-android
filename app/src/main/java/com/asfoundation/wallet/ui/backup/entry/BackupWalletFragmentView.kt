package com.asfoundation.wallet.ui.backup.entry

import io.reactivex.Observable

interface BackupWalletFragmentView {

  fun setupUi(walletAddress: String, symbol: String, formattedAmount: String)

  fun getBackupClick(): Observable<PasswordStatus>

  fun hideKeyboard()

  fun showPasswordFields()

  fun hidePasswordFields()

  fun onPasswordTextChanged(): Observable<PasswordFields>

  fun disableButton()

  fun enableButton()

  fun clearErrors()

  fun showPasswordError()

  fun onPasswordCheckedChanged(): Observable<Boolean>
}

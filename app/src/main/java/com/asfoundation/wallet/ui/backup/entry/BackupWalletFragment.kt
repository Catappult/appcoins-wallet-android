package com.asfoundation.wallet.ui.backup.entry

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_backup_wallet_layout.*
import kotlinx.android.synthetic.main.layout_backup_password_toggle.*
import kotlinx.android.synthetic.main.layout_wallet_backup_info.*
import javax.inject.Inject

class BackupWalletFragment : DaggerFragment(), BackupWalletFragmentView {

  @Inject
  lateinit var presenter: BackupWalletPresenter
  private var passwordSubject: PublishSubject<PasswordFields>? = null

  companion object {
    const val PARAM_WALLET_ADDR = "PARAM_WALLET_ADDR"

    @JvmStatic
    fun newInstance(walletAddress: String): BackupWalletFragment {
      val bundle = Bundle()
      bundle.putString(PARAM_WALLET_ADDR, walletAddress)
      val fragment = BackupWalletFragment()
      fragment.arguments = bundle
      return fragment
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    passwordSubject = PublishSubject.create()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_backup_wallet_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setToggleListener()
    setTextWatchers()
    presenter.present()
  }

  private fun setTextWatchers() {
    backup_password_edit_text.addTextChangedListener(
        PasswordTextWatcher(passwordSubject!!, backup_repeat_password_edit_text))
    backup_repeat_password_edit_text.addTextChangedListener(
        PasswordTextWatcher(passwordSubject!!, backup_password_edit_text))
  }

  private fun setToggleListener() {
    backup_password_toggle.setOnCheckedChangeListener { _, isChecked ->
      presenter.onCheckedChanged(isChecked)
    }
  }

  override fun setupUi(walletAddress: String, symbol: String, formattedAmount: String) {
    backup_wallet_address.text = walletAddress
    backup_balance.text = getString(R.string.value_fiat, symbol, formattedAmount)
  }

  override fun getBackupClick(): Observable<String> = RxView.clicks(backup_btn)
      .map { "password?.text.toString() " }

  override fun hideKeyboard() {
    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(password_group?.windowToken, 0)
  }

  override fun hidePasswordFields() {
    password_group.visibility = View.GONE
    backup_btn.isEnabled = true
  }

  override fun showPasswordFields() {
    password_group.visibility = View.VISIBLE
    if (areInvalidPasswordFields()) backup_btn.isEnabled = false
  }

  override fun onPasswordTextChanged(): Observable<PasswordFields> = passwordSubject!!

  private fun areInvalidPasswordFields(): Boolean {
    val password = backup_password_edit_text.text.toString()
    val repeatedPassword = backup_repeat_password_edit_text.text.toString()
    return password.isEmpty() || password != repeatedPassword
  }

  override fun disableButton() {
    backup_btn.isEnabled = false
  }

  override fun enableButton() {
    backup_btn.isEnabled = true
  }

  override fun clearErrors() {
    backup_repeat_password_input.error = null
  }

  override fun showPasswordError() {
    backup_repeat_password_input.error = "Password don't match"
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun onDestroy() {
    passwordSubject = null
    super.onDestroy()
  }
}

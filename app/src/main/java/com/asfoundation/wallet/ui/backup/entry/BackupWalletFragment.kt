package com.asfoundation.wallet.ui.backup.entry

import android.animation.LayoutTransition
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.common.WalletButtonView
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
  private var onPasswordCheckedSubject: PublishSubject<Boolean>? = null
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
    onPasswordCheckedSubject = PublishSubject.create()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_backup_wallet_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setToggleListener()
    setTextWatchers()
    setTransitionListener()
    presenter.present()
  }

  private fun setTextWatchers() {
    val passwordEditText = backup_password_edit_text
    val repeatPasswordEditText = backup_repeat_password_edit_text
    passwordEditText.addTextChangedListener(
        PasswordTextWatcher(passwordSubject!!, repeatPasswordEditText))
    repeatPasswordEditText.addTextChangedListener(
        PasswordTextWatcher(passwordSubject!!, passwordEditText))
  }

  private fun setToggleListener() {
    backup_password_toggle.setOnCheckedChangeListener { _, isChecked ->
      onPasswordCheckedSubject?.onNext(isChecked)
    }
  }

  override fun onPasswordCheckedChanged(): Observable<Boolean> = onPasswordCheckedSubject!!

  override fun setupUi(walletAddress: String, symbol: String, formattedAmount: String) {
    backup_wallet_address.text = walletAddress
    backup_balance.text = getString(R.string.value_fiat, symbol, formattedAmount)
  }

  override fun getBackupClick(): Observable<PasswordStatus> = RxView.clicks(backup_btn)
      .map {
        PasswordStatus(backup_password_edit_text.text.toString(), backup_password_toggle.isChecked)
      }

  override fun getSkipClick(): Observable<Any> = RxView.clicks(backup_skip_btn)

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
    if (areInvalidPasswordFields()) {
      backup_btn.isEnabled = false
    }
  }

  private fun areInvalidPasswordFields(): Boolean {
    val password = backup_password_edit_text.text.toString()
    val repeatedPassword = backup_repeat_password_edit_text.text.toString()
    return password.isEmpty() || password != repeatedPassword
  }

  override fun onPasswordTextChanged(): Observable<PasswordFields> = passwordSubject!!

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
    backup_repeat_password_input.error =
        getString(R.string.backup_additional_security_password_not_march)
  }

  private fun setTransitionListener() {
    backup_password_toggle_layout.layoutTransition.addTransitionListener(object :
        LayoutTransition.TransitionListener {
      override fun startTransition(transition: LayoutTransition?, container: ViewGroup?,
                                   view: View?, transitionType: Int) = Unit

      override fun endTransition(transition: LayoutTransition?, container: ViewGroup?,
                                 view: View?, transitionType: Int) {
        if (transitionType == LayoutTransition.APPEARING) {
          backup_scroll_view.smoothScrollTo(backup_scroll_view.x.toInt(), backup_scroll_view.bottom)
        }
      }
    })
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun onDestroy() {
    onPasswordCheckedSubject = null
    passwordSubject = null
    super.onDestroy()
  }
}

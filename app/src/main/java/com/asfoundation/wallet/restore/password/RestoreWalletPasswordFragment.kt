package com.asfoundation.wallet.restore.password

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.asf.wallet.R
import com.asfoundation.wallet.restore.RestoreWalletActivityView
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.RestoreErrorType
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_restore_wallet_password.*
import kotlinx.android.synthetic.main.wallet_outlined_card.*
import javax.inject.Inject

@AndroidEntryPoint
class RestoreWalletPasswordFragment : BasePageViewFragment(), RestoreWalletPasswordView {

  @Inject
  lateinit var currencyFormatUtils: CurrencyFormatUtils

  @Inject
  lateinit var presenter: RestoreWalletPasswordPresenter

  private lateinit var activityView: RestoreWalletActivityView

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is RestoreWalletActivityView) {
      throw IllegalStateException(
          "Restore Wallet Password fragment must be attached to Restore Wallet Activity")
    }
    activityView = context
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setTextChangeListener()
    presenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_restore_wallet_password, container, false)
  }

  override fun updateUi(address: String, fiatAmount: String, fiatSymbol: String) {
    wallet_address.text = address
    wallet_balance.text = getString(R.string.value_fiat, fiatSymbol, fiatAmount)
  }

  override fun restoreWalletButtonClick(): Observable<String> {
    return RxView.clicks(import_wallet_button)
        .map { password_edit_text.editableText.toString() }
  }

  override fun showWalletRestoreAnimation() = activityView.showWalletRestoreAnimation()

  override fun showWalletRestoredAnimation() = activityView.showWalletRestoredAnimation()

  override fun hideAnimation() = activityView.hideAnimation()

  override fun hideKeyboard() {
    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(password_edit_text.windowToken, 0)
  }

  override fun showError(type: RestoreErrorType) {
    label_input.isErrorEnabled = true
    when (type) {
      RestoreErrorType.ALREADY_ADDED -> label_input.error = getString(R.string.error_already_added)
      RestoreErrorType.INVALID_PASS -> label_input.error =
          getString(R.string.import_wallet_wrong_password_body)
      RestoreErrorType.INVALID_KEYSTORE -> label_input.error = getString(R.string.error_import)
      else -> label_input.error = getString(R.string.error_general)
    }
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  private fun setTextChangeListener() {
    password_edit_text.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
      override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
      override fun afterTextChanged(editable: Editable) {
        label_input.error = null
        label_input.isErrorEnabled = false
        import_wallet_button.isEnabled = editable.toString()
            .isNotEmpty()
      }
    })
  }

  companion object {

    const val KEYSTORE_KEY = "keystore"

    fun newInstance(keystore: String): RestoreWalletPasswordFragment {
      val fragment = RestoreWalletPasswordFragment()
      Bundle().apply {
        putString(KEYSTORE_KEY, keystore)
        fragment.arguments = this
      }
      return fragment
    }
  }

}

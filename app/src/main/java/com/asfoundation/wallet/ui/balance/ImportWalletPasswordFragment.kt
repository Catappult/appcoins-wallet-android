package com.asfoundation.wallet.ui.balance

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.util.ImportErrorType
import com.asfoundation.wallet.util.scaleToString
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.import_wallet_password_layout.*
import kotlinx.android.synthetic.main.wallet_outlined_card.*
import javax.inject.Inject

class ImportWalletPasswordFragment : DaggerFragment(), ImportWalletPasswordView {

  @Inject
  lateinit var importWalletPasswordInteractor: ImportWalletPasswordInteractor
  private lateinit var activityView: ImportWalletActivityView
  private lateinit var presenter: ImportWalletPasswordPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        ImportWalletPasswordPresenter(this, importWalletPasswordInteractor, CompositeDisposable(),
            AndroidSchedulers.mainThread(), Schedulers.io(), Schedulers.computation())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is ImportWalletActivityView) {
      throw IllegalStateException(
          "Import Wallet Password fragment must be attached to Import Wallet Activity")
    }
    activityView = context
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present(keystore)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.import_wallet_password_layout, container, false)
  }

  @SuppressLint("SetTextI18n")
  override fun updateUi(address: String, fiatValue: FiatValue) {
    setTextChangeListener()
    wallet_address.text = address
    wallet_balance.text = "${fiatValue.symbol}${fiatValue.amount.scaleToString(2)}"
  }

  override fun importWalletButtonClick(): Observable<String> {
    return RxView.clicks(import_wallet_button)
        .map { password_edit_text.editableText.toString() }
  }

  override fun showWalletImportAnimation() {
    activityView.showWalletImportAnimation()
  }

  override fun showWalletImportedAnimation() {
    activityView.showWalletImportedAnimation()
  }

  override fun hideAnimation() {
    activityView.hideAnimation()
  }

  override fun showError(type: ImportErrorType) {
    label_input.isErrorEnabled = true
    when (type) {
      ImportErrorType.ALREADY_ADDED -> label_input.error = getString(R.string.error_already_added)
      ImportErrorType.INVALID_PASS -> label_input.error =
          getString(R.string.import_wallet_wrong_password_body)
      ImportErrorType.INVALID_KEYSTORE -> label_input.error = "Invalid Keystore" //Needs strings
      else -> label_input.error = getString(R.string.error_general)
    }
  }

  private fun setTextChangeListener() {
    password_edit_text.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int,
                                     i2: Int) {
      }

      override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int,
                                 i2: Int) {
      }

      override fun afterTextChanged(editable: Editable) {
        label_input.isErrorEnabled = false
        import_wallet_button.isEnabled = editable.toString()
            .isNotEmpty()
      }
    })
  }

  private val keystore: String by lazy {
    if (arguments!!.containsKey(KEYSTORE_KEY)) {
      arguments!!.getString(KEYSTORE_KEY)
    } else {
      throw IllegalArgumentException("keystore not found")
    }
  }

  companion object {

    private const val KEYSTORE_KEY = "keystore"

    fun newInstance(keystore: String): ImportWalletPasswordFragment {
      val bundle = Bundle()
      val fragment = ImportWalletPasswordFragment()
      bundle.putString(KEYSTORE_KEY, keystore)
      fragment.arguments = bundle
      return fragment
    }

  }

}

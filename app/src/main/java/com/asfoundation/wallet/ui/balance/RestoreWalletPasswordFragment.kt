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
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.RestoreErrorType
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.restore_wallet_password_layout.*
import kotlinx.android.synthetic.main.wallet_outlined_card.*
import javax.inject.Inject

class RestoreWalletPasswordFragment : DaggerFragment(), RestoreWalletPasswordView {

  @Inject
  lateinit var restoreWalletPasswordInteractor: RestoreWalletPasswordInteractor

  @Inject
  lateinit var walletsEventSender: WalletsEventSender

  @Inject
  lateinit var currencyFormatUtils: CurrencyFormatUtils
  private lateinit var activityView: RestoreWalletActivityView
  private lateinit var presenter: RestoreWalletPasswordPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        RestoreWalletPasswordPresenter(this, activityView, restoreWalletPasswordInteractor,
            walletsEventSender, CompositeDisposable(), AndroidSchedulers.mainThread(),
            Schedulers.io(),
            Schedulers.computation())
  }

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
    presenter.present(keystore)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.restore_wallet_password_layout, container, false)
  }

  @SuppressLint("SetTextI18n")
  override fun updateUi(address: String, fiatValue: FiatValue) {
    setTextChangeListener()
    wallet_address.text = address
    wallet_balance.text =
        "${fiatValue.symbol}${currencyFormatUtils.formatCurrency(fiatValue.amount)}"
  }

  override fun restoreWalletButtonClick(): Observable<String> {
    return RxView.clicks(import_wallet_button)
        .map { password_edit_text.editableText.toString() }
  }

  override fun showWalletRestoreAnimation() {
    activityView.showWalletRestoreAnimation()
  }

  override fun showWalletRestoredAnimation() {
    activityView.showWalletRestoredAnimation()
  }

  override fun hideAnimation() {
    activityView.hideAnimation()
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

  private fun setTextChangeListener() {
    password_edit_text.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
      override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
      override fun afterTextChanged(editable: Editable) {
        label_input.isErrorEnabled = false
        import_wallet_button.isEnabled = editable.toString()
            .isNotEmpty()
      }
    })
  }

  private val keystore: String by lazy {
    if (arguments!!.containsKey(KEYSTORE_KEY)) {
      arguments!!.getString(KEYSTORE_KEY)!!
    } else {
      throw IllegalArgumentException("keystore not found")
    }
  }

  companion object {

    private const val KEYSTORE_KEY = "keystore"

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

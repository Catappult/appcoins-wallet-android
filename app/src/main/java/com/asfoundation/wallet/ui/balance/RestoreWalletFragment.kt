package com.asfoundation.wallet.ui.balance

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.interact.RestoreWalletInteractor
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.util.RestoreErrorType
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_restore_wallet_first_layout.*
import javax.inject.Inject

class RestoreWalletFragment : DaggerFragment(), RestoreWalletView {

  @Inject
  lateinit var restoreWalletInteractor: RestoreWalletInteractor

  @Inject
  lateinit var walletsEventSender: WalletsEventSender

  @Inject
  lateinit var logger: Logger
  private lateinit var activityView: RestoreWalletActivityView
  private lateinit var presenter: RestoreWalletPresenter

  companion object {
    private const val KEYSTORE = "keystore"

    @JvmStatic
    fun newInstance() = RestoreWalletFragment()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        RestoreWalletPresenter(this, activityView, CompositeDisposable(), restoreWalletInteractor,
            walletsEventSender, logger, AndroidSchedulers.mainThread(), Schedulers.computation())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is RestoreWalletActivityView) {
      throw IllegalStateException(
          "Restore Wallet fragment must be attached to Restore Wallet Activity")
    }
    activityView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_restore_wallet_first_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setTextChangeListener()
    savedInstanceState?.let { keystore_edit_text.setText(it.getString(KEYSTORE, "")) }
    presenter.present()
  }

  override fun restoreFromStringClick(): Observable<String> = RxView.clicks(import_wallet_button)
      .map { keystore_edit_text.editableText.toString() }

  override fun restoreFromFileClick() = RxView.clicks(import_from_file_button)


  override fun showError(type: RestoreErrorType) {
    label_input.isErrorEnabled = true
    when (type) {
      RestoreErrorType.ALREADY_ADDED -> label_input.error = getString(R.string.error_already_added)
      RestoreErrorType.INVALID_KEYSTORE -> label_input.error = getString(R.string.error_import)
      else -> label_input.error = getString(R.string.error_general)
    }
  }

  override fun navigateToPasswordView(keystore: String) {
    activityView.navigateToPasswordView(keystore)
  }

  private fun setTextChangeListener() {
    keystore_edit_text.addTextChangedListener(object : TextWatcher {
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

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    if (keystore_edit_text != null) {
      outState.putString(KEYSTORE, keystore_edit_text.editableText.toString())
    }
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}

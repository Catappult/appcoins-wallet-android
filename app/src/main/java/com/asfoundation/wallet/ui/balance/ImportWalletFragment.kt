package com.asfoundation.wallet.ui.balance

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.interact.ImportWalletInteract
import com.asfoundation.wallet.util.ImportErrorType
import com.google.gson.JsonObject
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.import_wallet_first_layout.*
import javax.inject.Inject

class ImportWalletFragment : DaggerFragment(), ImportWalletView {

  @Inject
  lateinit var importWalletInteract: ImportWalletInteract
  private lateinit var activityView: ImportWalletActivityView
  private lateinit var presenter: ImportWalletPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = ImportWalletPresenter(this, CompositeDisposable(), importWalletInteract,
        AndroidSchedulers.mainThread(), Schedulers.computation())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is ImportWalletActivityView) {
      throw IllegalStateException(
          "Import Wallet fragment must be attached to Import Wallet Activity")
    }
    activityView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.import_wallet_first_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setTextChangeListener()
    presenter.present()
  }

  override fun importFromStringClick(): Observable<String> {
    return RxView.clicks(import_wallet_button)
        .map { keystore_edit_text.editableText.toString() }
  }

  override fun importFromFileClick() = RxView.clicks(import_from_file_button)

  override fun launchFileIntent() {
    activityView.launchFileIntent()
  }

  override fun fileChosen() {
    activityView.fileChosen()
  }

  override fun hideAnimation() {
    activityView.hideAnimation()
  }

  override fun fileImported(): Observable<JsonObject> {
    return Observable.just(JsonObject()) //to change in future PRs
  }

  override fun showWalletImportAnimation() {
    activityView.showWalletImportAnimation()
  }

  override fun showWalletImportedAnimation() {
    activityView.showWalletImportedAnimation()
  }

  override fun showError(type: ImportErrorType) {
    label_input.isErrorEnabled = true
    when (type) {
      ImportErrorType.ALREADY_ADDED -> label_input.error = getString(R.string.error_already_added)
      ImportErrorType.INVALID_KEYSTORE -> label_input.error = "Invalid Keystore" //Needs strings
      else -> label_input.error = getString(R.string.error_general)
    }
  }

  override fun navigateToPasswordView() {
    val keystore = keystore_edit_text.editableText.toString()
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

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}

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

  companion object {
    private const val KEYSTORE = "keystore"

    @JvmStatic
    fun newInstance() = ImportWalletFragment()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        ImportWalletPresenter(this, activityView, CompositeDisposable(), importWalletInteract,
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
    savedInstanceState?.let { keystore_edit_text.setText(it.getString(KEYSTORE, "")) }
    presenter.present()
  }

  override fun importFromStringClick(): Observable<String> = RxView.clicks(import_wallet_button)
      .map { keystore_edit_text.editableText.toString() }

  override fun importFromFileClick() = RxView.clicks(import_from_file_button)


  override fun showError(type: ImportErrorType) {
    label_input.isErrorEnabled = true
    when (type) {
      ImportErrorType.ALREADY_ADDED -> label_input.error = getString(R.string.error_already_added)
      ImportErrorType.INVALID_KEYSTORE -> label_input.error = getString(R.string.error_import)
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
    outState.putString(KEYSTORE, keystore_edit_text.editableText.toString())
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}

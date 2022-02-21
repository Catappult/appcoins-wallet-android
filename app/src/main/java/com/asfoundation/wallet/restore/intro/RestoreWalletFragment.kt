package com.asfoundation.wallet.restore.intro

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
import com.asfoundation.wallet.util.RestoreErrorType
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_restore_wallet_first.*
import javax.inject.Inject

@AndroidEntryPoint
class RestoreWalletFragment : BasePageViewFragment(), RestoreWalletView {

  @Inject
  lateinit var presenter: RestoreWalletPresenter

  private lateinit var activityView: RestoreWalletActivityView

  companion object {

    @JvmStatic
    fun newInstance() = RestoreWalletFragment()
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
    return inflater.inflate(R.layout.fragment_restore_wallet_first, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setTextChangeListener()
    presenter.present(savedInstanceState)
  }

  override fun setKeystore(keystore: String) {
    keystore_edit_text.setText(keystore)
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

  override fun onPermissionsGiven() = activityView.onPermissionsGiven()

  override fun onFileChosen() = activityView.onFileChosen()

  override fun showWalletRestoreAnimation() = activityView.showWalletRestoreAnimation()

  override fun showWalletRestoredAnimation() = activityView.showWalletRestoredAnimation()

  override fun showSnackBarError() {
    Snackbar.make(fragment_main_view, R.string.unknown_error, Snackbar.LENGTH_SHORT)
        .show()
  }

  override fun hideAnimation() = activityView.hideAnimation()

  override fun askForReadPermissions() = activityView.askForReadPermissions()

  override fun hideKeyboard() {
    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(keystore_edit_text.windowToken, 0)
  }

  private fun setTextChangeListener() {
    keystore_edit_text.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
      override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
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
      presenter.onSaveInstanceState(outState, keystore_edit_text.editableText.toString())
    }
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}

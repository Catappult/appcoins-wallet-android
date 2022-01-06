package com.asfoundation.wallet.ui.backup.creation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.asf.wallet.R
import com.asfoundation.wallet.ui.backup.BackupActivityView
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.layout_backup_creation_options.*
import javax.inject.Inject

class BackupCreationFragment : BackupCreationView, DaggerFragment() {

  @Inject
  lateinit var presenter: BackupCreationPresenter
  private lateinit var activityView: BackupActivityView
  private lateinit var onWritePermissionGivenSubject: PublishSubject<Unit>

  companion object {

    const val WALLET_ADDRESS_KEY = "wallet_address"
    const val PASSWORD_KEY = "password"
    private const val RC_WRITE_EXTERNAL_STORAGE_PERMISSION = 1000

    @JvmStatic
    fun newInstance(walletAddress: String, password: String): BackupCreationFragment {
      val fragment = BackupCreationFragment()
      fragment.arguments = Bundle().apply {
        putString(WALLET_ADDRESS_KEY, walletAddress)
        putString(PASSWORD_KEY, password)
      }
      return fragment
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
    onWritePermissionGivenSubject = PublishSubject.create()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is BackupActivityView) { "Backup fragment must be attached to Backup activity" }
    activityView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_backup_creation_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    email_input.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        email_button.isEnabled = s.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(s)
            .matches()
      }

      override fun afterTextChanged(s: Editable) = Unit
    })
    presenter.present()
  }

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }

  override fun getSendToEmailClick(): Observable<String> {
    return RxView.clicks(email_button)
        .map { email_input.getText() }
  }

  override fun getSaveOnDeviceButton(): Observable<Any> = RxView.clicks(device_button)

  override fun showError() {
    Toast.makeText(context, R.string.error_export, Toast.LENGTH_LONG)
        .show()
    dismiss()
  }

  override fun dismiss() = activityView.closeScreen()

  override fun askForWritePermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
        ActivityCompat.checkSelfPermission(context!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
      onWritePermissionGivenSubject.onNext(Unit)
    } else {
      requestStorageWritePermission()
    }
  }

  private fun requestStorageWritePermission() {
    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        RC_WRITE_EXTERNAL_STORAGE_PERMISSION)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                          grantResults: IntArray) {
    if (requestCode == RC_WRITE_EXTERNAL_STORAGE_PERMISSION) {
      if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        onWritePermissionGivenSubject.onNext(Unit)
      }
    }
  }

  override fun onPermissionGiven(): Observable<Unit> = onWritePermissionGivenSubject
}

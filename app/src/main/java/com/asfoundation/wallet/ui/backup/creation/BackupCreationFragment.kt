package com.asfoundation.wallet.ui.backup.creation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import com.asf.wallet.R
import com.asfoundation.wallet.ui.backup.BackupActivityView
import com.asfoundation.wallet.ui.backup.SystemFileIntentResult
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.layout_backup_creation_options.*
import kotlinx.android.synthetic.main.layout_wallet_text_field_view.view.*
import kotlinx.android.synthetic.main.save_backup_layout.view.*
import javax.inject.Inject

class BackupCreationFragment : BackupCreationView, DaggerFragment() {

  @Inject
  lateinit var presenter: BackupCreationPresenter
  private lateinit var dialogView: View
  private lateinit var activityView: BackupActivityView
  private lateinit var dialog: AlertDialog
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
    dialogView = layoutInflater.inflate(R.layout.save_backup_layout, null)
    return inflater.inflate(R.layout.fragment_backup_creation_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present(savedInstanceState)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    presenter.onSaveInstanceState(outState)
  }

  override fun onResume() {
    super.onResume()
    presenter.onResume()
  }

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }

  override fun shareFile(uri: Uri) {
    activity?.let {
      ShareCompat.IntentBuilder.from(it)
          .setStream(uri)
          .setType("text/json")
          .setSubject(getString(R.string.tab_keystore))
          .setChooserTitle(R.string.share_via)
          .startChooser()
    }
  }

  override fun getFirstSaveClick(): Observable<Any> =
      Observable.never() // RxView.clicks(proceed_button)

  override fun getSendToEmailClick(): Observable<String> {
    return RxView.clicks(email_button)
        .map { email_input.getText() }
  }

  override fun getSaveOnDeviceButton(): Observable<Any> = RxView.clicks(device_button)

  override fun enableSaveButton() {
//    proceed_button.isEnabled = true
//    animation.cancelAnimation()
  }

  override fun showError() {
    Toast.makeText(context, R.string.error_export, Toast.LENGTH_LONG)
        .show()
    activityView.closeScreen()
  }

  override fun showSaveOnDeviceDialog(defaultName: String, path: String?) {
    if (!(::dialog.isInitialized)) {
      dialog = AlertDialog.Builder(context!!)
          .setView(dialogView)
          .create()
      dialog.window?.decorView?.setBackgroundResource(R.color.transparent)
      dialogView.visibility = View.VISIBLE
      dialogView.file_name_input.text_input?.setText(defaultName)
      path?.let {
        dialogView.store_path?.text = it
        dialogView.store_path?.visibility = View.VISIBLE
      }
    }
    dialog.show()
  }

  override fun showConfirmation() {
//    animation.visibility = View.INVISIBLE
//    backup_confirmation_image.setImageResource(R.drawable.ic_backup_confirm)
//    backup_confirmation_image.visibility = View.VISIBLE
//    title.text = getString(R.string.backup_done_body)
//    description.visibility = View.INVISIBLE
//    proceed_button.visibility = View.INVISIBLE
//    file_shared_buttons.visibility = View.VISIBLE
    //Fix for bug related with group layout
//    file_shared_buttons.requestLayout()
  }

  override fun getDialogCancelClick() = RxView.clicks(dialogView.backup_cancel)

  override fun getDialogSaveClick(): Observable<String> {
    return RxView.clicks(dialogView.backup_save)
        .map { dialogView.file_name_input.text_input.text.toString() }
  }

  override fun closeDialog() {
    if (::dialog.isInitialized) {
      dialog.cancel()
    }
  }

  override fun onSystemFileIntentResult(): Observable<SystemFileIntentResult> {
    return activityView.onSystemFileIntentResult()
  }

  override fun closeScreen() = activityView.closeScreen()

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

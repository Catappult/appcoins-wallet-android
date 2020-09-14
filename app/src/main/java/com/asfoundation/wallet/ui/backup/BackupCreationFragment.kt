package com.asfoundation.wallet.ui.backup

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ShareCompat
import com.asf.wallet.R
import com.asfoundation.wallet.backup.FileInteractor
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.interact.ExportWalletInteract
import com.asfoundation.wallet.logging.Logger
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.backup_dialog.view.*
import kotlinx.android.synthetic.main.fragment_backup_creation_layout.*
import javax.inject.Inject

class BackupCreationFragment : BackupCreationView, DaggerFragment() {

  @Inject
  lateinit var exportWalletInteract: ExportWalletInteract

  @Inject
  lateinit var fileInteractor: FileInteractor

  @Inject
  lateinit var walletsEventSender: WalletsEventSender

  @Inject
  lateinit var logger: Logger

  private lateinit var dialogView: View
  private lateinit var presenter: BackupCreationPresenter
  private lateinit var activityView: BackupActivityView
  private lateinit var dialog: AlertDialog

  companion object {

    private const val WALLET_ADDRESS_KEY = "wallet_address"
    private const val PASSWORD_KEY = "password"

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

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is BackupActivityView) { "Backup fragment must be attached to Backup activity" }
    activityView = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        BackupCreationPresenter(activityView, this, exportWalletInteract, fileInteractor,
            walletsEventSender, logger,
            Schedulers.io(), AndroidSchedulers.mainThread(), CompositeDisposable(), walletAddress,
            password, fileInteractor.getTemporaryPath(), fileInteractor.getDownloadPath())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    dialogView = layoutInflater.inflate(R.layout.backup_dialog, null)
    return inflater.inflate(R.layout.fragment_backup_creation_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    proceed_button.visibility = View.VISIBLE //To avoid flick when user navigates with open keyboard
    presenter.present(savedInstanceState)
    animation.playAnimation()
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

  override fun getFirstSaveClick() = RxView.clicks(proceed_button)

  override fun getFinishClick(): Observable<Any> = RxView.clicks(finish_button)

  override fun getSaveAgainClick() = RxView.clicks(save_again_button)

  override fun enableSaveButton() {
    proceed_button.isEnabled = true
    animation.cancelAnimation()
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
      dialogView.edit_text_name?.setText(defaultName)
      path?.let {
        dialogView.store_path?.text = it
        dialogView.store_path?.visibility = View.VISIBLE
      }
    }
    dialog.show()
  }

  override fun showConfirmation() {
    animation.visibility = View.INVISIBLE
    backup_confirmation_image.setImageResource(R.drawable.ic_backup_confirm)
    backup_confirmation_image.visibility = View.VISIBLE
    title.text = getString(R.string.backup_done_body)
    description.visibility = View.INVISIBLE
    proceed_button.visibility = View.INVISIBLE
    file_shared_buttons.visibility = View.VISIBLE
    //Fix for bug related with group layout
    file_shared_buttons.requestLayout()
  }

  override fun getDialogCancelClick() = RxView.clicks(dialogView.backup_cancel)

  override fun getDialogSaveClick(): Observable<String> {
    return RxView.clicks(dialogView.backup_save)
        .map { dialogView.edit_text_name.text.toString() }
  }

  override fun closeDialog() {
    if (::dialog.isInitialized) {
      dialog.cancel()
    }
  }

  private val walletAddress: String by lazy {
    if (arguments!!.containsKey(WALLET_ADDRESS_KEY)) {
      arguments!!.getString(WALLET_ADDRESS_KEY)!!
    } else {
      throw IllegalArgumentException("Wallet address not available")
    }
  }

  private val password: String by lazy {
    if (arguments!!.containsKey(PASSWORD_KEY)) {
      arguments!!.getString(PASSWORD_KEY)!!
    } else {
      throw IllegalArgumentException("Password not available")
    }
  }
}

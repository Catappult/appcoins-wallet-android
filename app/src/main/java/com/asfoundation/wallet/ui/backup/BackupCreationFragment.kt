package com.asfoundation.wallet.ui.backup

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import com.asf.wallet.R
import com.asfoundation.wallet.backup.FileInteract
import com.asfoundation.wallet.interact.ExportWalletInteract
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_backup_creation_layout.*
import javax.inject.Inject

class BackupCreationFragment : BackupCreationView, DaggerFragment() {

  @Inject
  lateinit var exportWalletInteract: ExportWalletInteract

  @Inject
  lateinit var fileInteract: FileInteract
  private lateinit var presenter: BackupCreationPresenter
  private lateinit var activityView: BackupActivityView

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
    check(context is BackupActivityView) { "TopUp fragment must be attached to TopUp activity" }
    activityView = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = BackupCreationPresenter(activityView, this, exportWalletInteract, fileInteract,
        Schedulers.io(), AndroidSchedulers.mainThread())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_backup_creation_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    proceed_btn.visibility = View.VISIBLE
    presenter.presenter(walletAddress, password,
        context!!.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!)
    animation.playAnimation()
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
    ShareCompat.IntentBuilder.from(activity)
        .setStream(uri)
        .setType("text/plain")
        .setChooserTitle(R.string.askafriend_share_popup_title)
        .startChooser()
  }

  override fun getPositiveButtonClick() = RxView.clicks(proceed_btn)

  override fun getNegativeButtonClick() = RxView.clicks(done_btn)

  override fun enableSaveButton() {
    proceed_btn.isEnabled = true
    animation.cancelAnimation()
  }

  override fun showError() {

  }

  override fun showConfirmation() {
    animation.visibility = View.INVISIBLE
    backup_confirmation_image.setImageResource(R.drawable.ic_backup_confirm)
    backup_confirmation_image.visibility = View.VISIBLE
    title.setText(R.string.backup_done_body)
    description.visibility = View.INVISIBLE
    done_btn.visibility = View.VISIBLE
    proceed_btn.text = getText(R.string.backup_confirmation_yes)
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
      throw IllegalArgumentException("Wallet address not available")
    }
  }
}

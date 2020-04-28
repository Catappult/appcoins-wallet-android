package com.asfoundation.wallet.ui.backup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_backup_creation_layout.*

class BackupCreationFragment: BackupCreationView, DaggerFragment() {

  private lateinit var fragmentContainer: ViewGroup
  private lateinit var presenter: BackupCreationPresenter

  companion object {
    private const val PARAM_WALLET_ADDR = "PARAM_WALLET_ADDR"

    @JvmStatic
    fun newInstance(): BackupCreationFragment {
      return BackupCreationFragment()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = BackupCreationPresenter(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    fragmentContainer = container!!
    return inflater.inflate(R.layout.fragment_backup_creation_layout, container, false)
  }

  override fun onResume() {
    super.onResume()
    Log.e("BackupCreationFragment", "onResume")
    presenter.onResume()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.presenter()
    animation.playAnimation()
  }

  override fun shareFile(uri: String) {
    ShareCompat.IntentBuilder.from(activity)
        .setText(uri)
        .setType("text/*")
        .setChooserTitle(R.string.askafriend_share_popup_title)
        .startChooser()
  }

  override fun getPositiveButtonClick(): Observable<Any> {
    return RxView.clicks(proceed_btn)
  }

  override fun getNegativeButtonClick(): Observable<Any> {
    return RxView.clicks(done_btn)
  }

  override fun showConfirmation() {
    animation.cancelAnimation()
    animation.visibility = View.INVISIBLE
    img.setImageResource(R.drawable.ic_bkp_confirm)
    img.visibility =  View.VISIBLE
    title.setText(R.string.backup_done_title)
    description.setText(R.string.backup_done_body)
    done_btn.visibility = View.VISIBLE
    proceed_btn.text = getText(R.string.backup_confirmation_yes)
  }


}

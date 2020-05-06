package com.asfoundation.wallet.ui.backup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_backup_creation_layout.*

class BackupCreationFragment : BackupCreationView, DaggerFragment() {

  private lateinit var presenter: BackupCreationPresenter
  private lateinit var activityView: BackupActivityView

  companion object {
    @JvmStatic
    fun newInstance() = BackupCreationFragment()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = BackupCreationPresenter(activityView, this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_backup_creation_layout, container, false)
  }

  override fun onResume() {
    super.onResume()
    presenter.onResume()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.presenter()
    animation.playAnimation()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is BackupActivityView) { "TopUp fragment must be attached to TopUp activity" }
    activityView = context
  }

  override fun shareFile(uri: String) {
    ShareCompat.IntentBuilder.from(activity)
        .setText(uri)
        .setType("text/*")
        .setChooserTitle(R.string.askafriend_share_popup_title)
        .startChooser()
  }

  override fun getPositiveButtonClick() = RxView.clicks(proceed_btn)

  override fun getNegativeButtonClick() = RxView.clicks(done_btn)

  override fun showConfirmation() {
    animation.cancelAnimation()
    animation.visibility = View.INVISIBLE
    backup_confirmation_image.setImageResource(R.drawable.ic_backup_confirm)
    backup_confirmation_image.visibility = View.VISIBLE
    title.setText(R.string.backup_done_title)
    description.setText(R.string.backup_done_body)
    done_btn.visibility = View.VISIBLE
    proceed_btn.text = getText(R.string.backup_confirmation_yes)
  }
}

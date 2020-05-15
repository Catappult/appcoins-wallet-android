package com.asfoundation.wallet.ui.backup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_backup_creation_layout.animation
import kotlinx.android.synthetic.main.fragment_backup_success_layout.*

class BackupSuccessFragment : DaggerFragment(), BackupSuccessFragmentView {

  private lateinit var presenter: BackupSuccessPresenter
  private lateinit var activityView: BackupActivityView

  companion object {
    @JvmStatic
    fun newInstance() = BackupSuccessFragment()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = BackupSuccessPresenter(this, activityView, CompositeDisposable())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_backup_success_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
    animation.playAnimation()
    val text = "${getString(R.string.backup_confirmation_tips_title)}\n\n• ${getString(
        R.string.backup_confirmation_tips_1)}\n• ${getString(
        R.string.backup_confirmation_tips_2)}\n• ${getString(
        R.string.backup_confirmation_tips_3)}"
    information.text = text
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(
        context is BackupActivityView) { "BackupSuccess fragment must be attached to Backup activity" }
    activityView = context
  }

  override fun getCloseButtonClick() = RxView.clicks(close_btn)

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }
}

package com.asfoundation.wallet.ui.backup.success

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.ui.backup.BackupActivityView
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_backup_success_layout.*
import kotlinx.android.synthetic.main.layout_backup_success_info.view.*
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BackupSuccessFragment : DaggerFragment(), BackupSuccessFragmentView {

  @Inject
  lateinit var presenter: BackupSuccessPresenter
  private lateinit var activityView: BackupActivityView

  companion object {
    const val EMAIL_KEY = "email"

    @JvmStatic
    fun newInstance(email: Boolean): BackupSuccessFragment {
      return BackupSuccessFragment()
          .apply {
            arguments = Bundle().apply {
              putBoolean(EMAIL_KEY, email)
            }
          }
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_backup_success_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(
        context is BackupActivityView) { "BackupSuccess fragment must be attached to Backup activity" }
    activityView = context
  }

  override fun getCloseButtonClick() = RxView.clicks(close_button)

  override fun closeScreen() = activityView.closeScreen()

  override fun setSuccessInfo(info: String) {
    backup_success_info.body.text = info
  }

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }
}

package com.asfoundation.wallet.ui.backup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_backup_creation_layout.animation
import kotlinx.android.synthetic.main.fragment_backup_success_layout.*

class BackupSuccessFragment: DaggerFragment(), BackupSuccessFragmentView {

  private lateinit var fragmentContainer: ViewGroup
  private lateinit var presenter: BackupSuccessFragmentPresenter
  private lateinit var activityView: BackupActivityView

  companion object {
    @JvmStatic
    fun newInstance(): BackupSuccessFragment {
      return BackupSuccessFragment()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = BackupSuccessFragmentPresenter(this, activityView)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    fragmentContainer = container!!
    return inflater.inflate(R.layout.fragment_backup_success_layout, container, false)
  }

  override fun onResume() {
    super.onResume()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
    animation.playAnimation()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(
        context is BackupActivityView) { "TopUp fragment must be attached to TopUp activity" }
    activityView = context
  }

  override fun getCloseButtonClick(): Observable<Any> {
    return RxView.clicks(close_btn)
  }


}

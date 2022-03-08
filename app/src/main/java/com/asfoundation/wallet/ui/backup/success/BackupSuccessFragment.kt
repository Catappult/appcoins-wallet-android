package com.asfoundation.wallet.ui.backup.success

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentBackupSuccessLayoutBinding
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BackupSuccessFragment : BasePageViewFragment(),
  SingleStateFragment<ViewState, SideEffect> {

  private val views by viewBinding(FragmentBackupSuccessLayoutBinding::bind)

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

    views.closeButton.setOnClickListener {
      this.activity?.finish()
    }

    setSuccessInfo()
  }

  private fun setSuccessInfo() {
    var info = "Your backup file is stored in your device"

    if (requireArguments().getBoolean(EMAIL_KEY)) {
      info = "Your backup file is in your email"
    }

    views.backupSuccessInfo.body.text = info
  }

  override fun onStateChanged(state: ViewState) = Unit

  override fun onSideEffect(sideEffect: SideEffect) = Unit
}

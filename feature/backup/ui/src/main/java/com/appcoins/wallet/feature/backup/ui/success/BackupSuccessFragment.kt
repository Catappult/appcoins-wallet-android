package com.appcoins.wallet.feature.backup.ui.success

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsAnalytics
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsEventSender
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.feature.backup.ui.R
import com.appcoins.wallet.feature.backup.ui.databinding.BackupSuccessFragmentBinding
import com.appcoins.wallet.feature.backup.ui.triggers.TriggerUtils.toJson
import com.appcoins.wallet.sharedpreferences.BackupTriggerPreferencesDataSource
import com.appcoins.wallet.sharedpreferences.BackupTriggerPreferencesDataSource.TriggerSource.DISABLED
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupSuccessFragment : BasePageViewFragment(),
  SingleStateFragment<ViewState, SideEffect> {

  @Inject
  lateinit var backupTriggerPreferences: BackupTriggerPreferencesDataSource

  @Inject
  lateinit var walletsEventSender: WalletsEventSender

  private val views by viewBinding(BackupSuccessFragmentBinding::bind)

  companion object {
    const val EMAIL_KEY = "email"
    const val WALLET_ADDRESS_KEY = "wallet_address_key"

    @JvmStatic
    fun newInstance(walletAddress: String, email: Boolean): BackupSuccessFragment {
      return BackupSuccessFragment()
        .apply {
          arguments = Bundle().apply {
            putString(WALLET_ADDRESS_KEY, walletAddress)
            putBoolean(EMAIL_KEY, email)
          }
        }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? = BackupSuccessFragmentBinding.inflate(layoutInflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    backupTriggerPreferences.setTriggerState(
      walletAddress = requireArguments().getString(WALLET_ADDRESS_KEY, ""),
      active = false,
      triggerSource = DISABLED.toJson()
    )

    views.closeButton.setOnClickListener {
      walletsEventSender.sendBackupConclusionEvent(
        WalletsAnalytics.ACTION_UNDERSTAND
      )
      this.activity?.finish()
    }

    setSuccessInfo()
  }

  private fun setSuccessInfo() {
    var info = R.string.backup_success_save_on_device

    if (requireArguments().getBoolean(EMAIL_KEY)) {
      info = R.string.backup_success_save_on_email
    }

    views.backupSuccessInfo.body.text = context?.getString(info)
  }

  override fun onStateChanged(state: ViewState) = Unit

  override fun onSideEffect(sideEffect: SideEffect) = Unit
}

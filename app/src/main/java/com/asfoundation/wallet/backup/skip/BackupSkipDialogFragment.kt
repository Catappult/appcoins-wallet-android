package com.asfoundation.wallet.backup.skip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.BackupSkipDialogFragmentBinding
import com.asfoundation.wallet.backup.repository.preferences.BackupTriggerPreferences
import com.asfoundation.wallet.backup.triggers.BackupTriggerDialogFragment
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.appcoins.wallet.ui.arch.ViewState
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupSkipDialogFragment : BottomSheetDialogFragment(),
  SingleStateFragment<ViewState, SideEffect> {


  @Inject
  lateinit var navigator: BackupSkipDialogNavigator

  @Inject
  lateinit var backupTriggerPreferences: BackupTriggerPreferences

  private val views by viewBinding(BackupSkipDialogFragmentBinding::bind)

  companion object {
    @JvmStatic
    fun newInstance(
      walletAddress: String,
      triggerSource: BackupTriggerPreferences.TriggerSource
    ): BackupSkipDialogFragment {
      return BackupSkipDialogFragment()
        .apply {
          arguments = Bundle().apply {
            putString(BackupTriggerDialogFragment.WALLET_ADDRESS_KEY, walletAddress)
            putSerializable(BackupTriggerDialogFragment.TRIGGER_SOURCE, triggerSource)
          }
        }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.backup_skip_dialog_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.confirm.setOnClickListener {
      val walletAddress =
        requireArguments().getString(BackupTriggerDialogFragment.WALLET_ADDRESS_KEY)!!
      backupTriggerPreferences.setBackupTriggerSeenTime(walletAddress, System.currentTimeMillis())
      navigator.finishBackup(walletAddress)
    }
    views.cancel.setOnClickListener {
      navigator.navigateBack(
        requireArguments().getString(BackupTriggerDialogFragment.WALLET_ADDRESS_KEY)!!,
        requireArguments().getSerializable(BackupTriggerDialogFragment.TRIGGER_SOURCE)!! as BackupTriggerPreferences.TriggerSource
      )
    }
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int {
    return R.style.AppBottomSheetDialogThemeNotDraggable
  }

  override fun onStateChanged(state: ViewState) = Unit

  override fun onSideEffect(sideEffect: SideEffect) = Unit
}
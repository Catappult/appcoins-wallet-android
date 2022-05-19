package com.asfoundation.wallet.backup.triggers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.BackupTriggerDialogFragmentBinding
import com.asfoundation.wallet.backup.repository.preferences.BackupTriggerPreferences
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupTriggerDialogFragment : BottomSheetDialogFragment(),
  SingleStateFragment<ViewState, SideEffect> {

  @Inject
  lateinit var navigator: BackupTriggerDialogNavigator

  @Inject
  lateinit var walletsEventSender: WalletsEventSender

  private val views by viewBinding(BackupTriggerDialogFragmentBinding::bind)

  companion object {
    const val WALLET_ADDRESS_KEY = "wallet_address"
    const val TRIGGER_SOURCE = "trigger_source"

    @JvmStatic
    fun newInstance(
      walletAddress: String,
      triggerSource: BackupTriggerPreferences.TriggerSource
    ): BackupTriggerDialogFragment {
      return BackupTriggerDialogFragment()
        .apply {
          arguments = Bundle().apply {
            putString(WALLET_ADDRESS_KEY, walletAddress)
            putSerializable(TRIGGER_SOURCE, triggerSource)
          }
        }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.backup_trigger_dialog_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setUIContext()
    setListeners()
  }

  private fun setUIContext() {
    when (requireArguments().getSerializable(TRIGGER_SOURCE)!!) {
      BackupTriggerPreferences.TriggerSource.NEW_LEVEL -> views.triggerDialogMessage.text =
        getString(R.string.backup_popup_gamification_body)
      BackupTriggerPreferences.TriggerSource.FIRST_PURCHASE -> views.triggerDialogMessage.text =
        getString(R.string.backup_popup_purchase_body)
      else -> {
      }
    }
  }

  private fun setListeners() {
    views.triggerBackupBtn.setOnClickListener {
      walletsEventSender.sendCreateBackupEvent(
        null,
        WalletsAnalytics.BACKUP_TRIGGER,
        null
      )
      navigator.navigateToBackupActivity(
        requireArguments().getString(WALLET_ADDRESS_KEY)!!
      )
    }
    views.triggerDismissBtn.setOnClickListener {
      navigator.navigateToDismiss(
        requireArguments().getString(WALLET_ADDRESS_KEY)!!,
        requireArguments().getSerializable(TRIGGER_SOURCE)!! as BackupTriggerPreferences.TriggerSource
      )
    }
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int {
    return R.style.AppBottomSheetDialogThemeNoFloating
  }

  override fun onStateChanged(state: ViewState) = Unit

  override fun onSideEffect(sideEffect: SideEffect) = Unit
}
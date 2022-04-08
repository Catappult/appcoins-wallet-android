package com.asfoundation.wallet.backup.triggers

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.BackupTriggerDialogFragmentBinding
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.base.ViewState
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupTriggerDialogFragment : BottomSheetDialogFragment(),
  SingleStateFragment<ViewState, SideEffect> {

  @Inject
  lateinit var navigator: BackupTriggerDialogNavigator

  private val views by viewBinding(BackupTriggerDialogFragmentBinding::bind)

  companion object {
    const val WALLET_ADDRESS_KEY = "wallet_address"
    const val TRIGGER_SOURCE = "trigger_source"

    @JvmStatic
    fun newInstance(walletAddress: String, triggerSource: String): BackupTriggerDialogFragment {
      return BackupTriggerDialogFragment()
        .apply {
          arguments = Bundle().apply {
            putString(WALLET_ADDRESS_KEY, walletAddress)
            putString(TRIGGER_SOURCE, triggerSource)
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
    Log.d(
      "APPC-2782",
      "BackupTriggerDialogFragment: onViewCreated: source -> ${
        requireArguments().getString(TRIGGER_SOURCE)!!
      } "
    )

    views.triggerDialogWallet.text =
      requireArguments().getString(WALLET_ADDRESS_KEY)!!

    //TODO
    when (requireArguments().getString(TRIGGER_SOURCE)!!) {
      "NewLevel" -> views.triggerDialogMessage.text =
        "(Example) Congrats on reaching a new level, we recommend you to backup your wallet to avoid losing your progress."
      "FirstPurchase" -> views.triggerDialogMessage.text =
        "(Example) Congrats on your first purchase, we recommend you to backup your wallet to avoid the bonus your received"
      else -> views.triggerDialogMessage.text = ""
    }
  }

  private fun setListeners() {
    views.triggerBackupBtn.setOnClickListener {
      navigator.navigateToBackupActivity(
        requireArguments().getString(WALLET_ADDRESS_KEY)!!
      )
    }
    views.triggerDismissBtn.setOnClickListener {
      navigator.navigateToDismiss(
        requireArguments().getString(WALLET_ADDRESS_KEY)!!,
        requireArguments().getString(TRIGGER_SOURCE)!!
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
package com.asfoundation.wallet.backup.save_options

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.asf.wallet.databinding.BackupSaveOptionsFragmentBinding
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupSaveOptionsFragment : BasePageViewFragment(),
  SingleStateFragment<BackupSaveOptionsState, BackupSaveOptionsSideEffect> {

  @Inject
  lateinit var backupSaveOptionsViewModelFactory: BackupSaveOptionsViewModelFactory

  @Inject
  lateinit var navigator: BackupSaveOptionsNavigator

  @Inject
  lateinit var walletsEventSender: WalletsEventSender

  private val viewModel: BackupSaveOptionsViewModel by viewModels { backupSaveOptionsViewModelFactory }
  private val views by viewBinding(BackupSaveOptionsFragmentBinding::bind)

  companion object {

    const val WALLET_ADDRESS_KEY = "wallet_address"
    const val PASSWORD_KEY = "password"

    @JvmStatic
    fun newInstance(walletAddress: String, password: String): BackupSaveOptionsFragment {
      return BackupSaveOptionsFragment()
        .apply {
          arguments = Bundle().apply {
            putString(WALLET_ADDRESS_KEY, walletAddress)
            putString(PASSWORD_KEY, password)
          }
        }
    }
  }


  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = BackupSaveOptionsFragmentBinding.inflate(layoutInflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.backupCreationOptions.emailInput.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        views.backupCreationOptions.emailButton.isEnabled =
          s.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(s)
            .matches()
      }

      override fun afterTextChanged(s: Editable) = Unit
    })
    views.backupCreationOptions.emailButton.setOnClickListener {
      walletsEventSender.sendBackupConfirmationEvent(
        WalletsAnalytics.ACTION_SEND_EMAIL
      )
      viewModel.sendBackupToEmail(views.backupCreationOptions.emailInput?.getText() ?: "")
    }
    views.backupCreationOptions.emailButton.isEnabled =
      false // this needs to be after setOnClickListener, otherwise button will be clickable
    views.backupCreationOptions.deviceButton.setOnClickListener {
      walletsEventSender.sendBackupConfirmationEvent(
        WalletsAnalytics.ACTION_SAVE
      )
      navigator.navigateToSaveOnDeviceScreen(
        requireArguments().getString(WALLET_ADDRESS_KEY)!!,
        requireArguments().getString(
          PASSWORD_KEY
        )!!
      )
    }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  fun showError() {
    Toast.makeText(context, R.string.error_export, Toast.LENGTH_LONG)
      .show()
    requireActivity().finish()
  }

  override fun onStateChanged(state: BackupSaveOptionsState) = Unit

  override fun onSideEffect(sideEffect: BackupSaveOptionsSideEffect) =
    when (sideEffect) {
      is BackupSaveOptionsSideEffect.NavigateToSuccess -> navigator.navigateToSuccessScreen(
        sideEffect.walletAddress
      )
      BackupSaveOptionsSideEffect.ShowError -> showError()
    }
}

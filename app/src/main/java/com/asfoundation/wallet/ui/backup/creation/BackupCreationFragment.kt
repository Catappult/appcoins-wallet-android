package com.asfoundation.wallet.ui.backup.creation

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
import com.asf.wallet.databinding.LayoutBackupCreationOptionsBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupCreationFragment : BasePageViewFragment(),
    SingleStateFragment<BackupCreationState, BackupCreationSideEffect> {

  @Inject
  lateinit var backupCreationViewModelFactory: BackupCreationViewModelFactory

  @Inject
  lateinit var navigator: BackupCreationNavigator

  private val viewModel: BackupCreationViewModel by viewModels { backupCreationViewModelFactory }
  private val views by viewBinding(LayoutBackupCreationOptionsBinding::bind)

  companion object {

    const val WALLET_ADDRESS_KEY = "wallet_address"
    const val PASSWORD_KEY = "password"

    @JvmStatic
    fun newInstance(walletAddress: String, password: String): BackupCreationFragment {
      return BackupCreationFragment()
          .apply {
            arguments = Bundle().apply {
              putString(WALLET_ADDRESS_KEY, walletAddress)
              putString(PASSWORD_KEY, password)
            }
          }
    }
  }


  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_backup_creation_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.emailInput.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        views.emailButton.isEnabled =
            s.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(s)
                .matches()
      }

      override fun afterTextChanged(s: Editable) = Unit
    })
    views.emailButton.setOnClickListener {
      viewModel.sendBackupToEmail(views.emailInput.getText())
    }
    views.emailButton.isEnabled =
        false // this needs to be after setOnClickListener, otherwise button will be clickable
    views.deviceButton.setOnClickListener {
      navigator.navigateToSaveOnDeviceScreen(requireArguments().getString(WALLET_ADDRESS_KEY)!!,
          requireArguments().getString(
              PASSWORD_KEY)!!)
    }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  fun showError() {
    Toast.makeText(context, R.string.error_export, Toast.LENGTH_LONG)
        .show()
    requireActivity().finish()
  }

  override fun onStateChanged(state: BackupCreationState) = Unit

  override fun onSideEffect(sideEffect: BackupCreationSideEffect) = when (sideEffect) {
    BackupCreationSideEffect.ShowError -> showError()
    BackupCreationSideEffect.NavigateToSuccess -> navigator.navigateToSuccessScreen()
  }
}

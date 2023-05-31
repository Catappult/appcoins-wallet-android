package com.asfoundation.wallet.recover.entry

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.RecoverEntryFragmentBinding
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.asfoundation.wallet.my_wallets.create_wallet.CreateWalletDialogFragment
import com.asfoundation.wallet.recover.RecoverActivity.Companion.ONBOARDING_LAYOUT
import com.asfoundation.wallet.recover.result.FailedEntryRecover
import com.asfoundation.wallet.recover.result.RecoverEntryResult
import com.asfoundation.wallet.recover.result.SuccessfulEntryRecover
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RecoverEntryFragment : BasePageViewFragment(),
  SingleStateFragment<RecoverEntryState, RecoverEntrySideEffect> {

  @Inject
  lateinit var navigator: RecoverEntryNavigator

  private val viewModel: RecoverEntryViewModel by viewModels()
  private val views by viewBinding(RecoverEntryFragmentBinding::bind)

  private lateinit var requestPermissionsLauncher: ActivityResultLauncher<String>
  private lateinit var storageIntentLauncher: ActivityResultLauncher<Intent>

  override fun onCreateView(
    inflater: LayoutInflater, @Nullable container: ViewGroup?,
    @Nullable savedInstanceState: Bundle?
  ): View = RecoverEntryFragmentBinding.inflate(inflater).root

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    createLaunchers()
  }

  override fun onResume() {
    super.onResume()
    handleFragmentResult()
  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    //needed to handle the removal of activity usage from onboarding
    val isOnboardingLayout = requireArguments().getBoolean(ONBOARDING_LAYOUT, false)
    if (!isOnboardingLayout) {
      views.recoverWalletBackButton.visibility = View.GONE
    }
    views.recoverWalletBackButton.setOnClickListener {
      navigator.navigateBack(fromActivity = !isOnboardingLayout)
    }
    views.recoverWalletOptions.recoverFromFileButton.setOnClickListener {
      // For Android 33 and beyond, the READ_EXTERNAL_STORAGE permission does not work. Though it's
      // still needed for backward compatibility.
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        requestPermissionsLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
      } else {
        navigator.launchFileIntent(storageIntentLauncher, viewModel.filePath())
      }
    }
    views.recoverWalletButton.setOnClickListener {
      viewModel.handleRecoverClick(
        views.recoverWalletOptions.recoverKeystoreInput.getText().trim()
      )
    }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  private fun createLaunchers() {
    requestPermissionsLauncher =
      registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
          navigator.launchFileIntent(storageIntentLauncher, viewModel.filePath())
        }
      }
    storageIntentLauncher =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
          result.data?.let {
            viewModel.handleFileChosen(uri = it.data ?: Uri.parse(""))
          }
        }
      }
  }

  override fun onStateChanged(state: RecoverEntryState) {
    handleRecoverEntryState(state.recoverResultAsync)
  }

  override fun onSideEffect(sideEffect: RecoverEntrySideEffect) = Unit

  private fun handleRecoverEntryState(asyncRecoverResult: Async<RecoverEntryResult>) {
    when (asyncRecoverResult) {
      is Async.Uninitialized,
      is Async.Loading -> {
        showLoading()
      }
      is Async.Fail -> {
        handleErrorState(FailedEntryRecover.GenericError(asyncRecoverResult.error.throwable))
      }
      is Async.Success -> {
        handleSuccessState(asyncRecoverResult())
      }
    }
  }

  fun showLoading() {

  }

  private fun handleSuccessState(recoverResult: RecoverEntryResult) {
    when (recoverResult) {
      is SuccessfulEntryRecover -> {
        navigator.navigateToCreateWalletDialog(requireArguments().getBoolean(ONBOARDING_LAYOUT, false))
      }
      else -> handleErrorState(recoverResult)
    }
  }

  private fun handleErrorState(recoverResult: RecoverEntryResult) {
    when (recoverResult) {
      is FailedEntryRecover.AlreadyAdded -> {
        views.recoverWalletOptions.recoverKeystoreInput.setError(getString(R.string.error_already_added))
      }
      is FailedEntryRecover.InvalidKeystore -> {
        views.recoverWalletOptions.recoverKeystoreInput.setError(getString(R.string.error_import))
      }
      is FailedEntryRecover.InvalidPassword -> {
        navigator.navigateToRecoverPasswordFragment(
          keystore = recoverResult.keyStore,
          walletBalance = recoverResult.symbol + recoverResult.amount,
          walletAddress = recoverResult.address,
          requireArguments().getBoolean(ONBOARDING_LAYOUT, false)
        )
      }
      is FailedEntryRecover.InvalidPrivateKey -> {
        views.recoverWalletOptions.recoverKeystoreInput.setError(getString(R.string.error_import))
      }
      is FailedEntryRecover.GenericError -> {
        views.recoverWalletOptions.recoverKeystoreInput.setError(getString(R.string.error_general))
      }
      else -> return
    }
  }

  private fun handleFragmentResult() {
    parentFragmentManager.setFragmentResultListener(
      CreateWalletDialogFragment.CREATE_WALLET_DIALOG_COMPLETE,
      this
    ) { _, _ ->
      navigator.navigateToNavigationBar()
    }
  }
}
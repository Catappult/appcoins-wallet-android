package com.asfoundation.wallet.recover.entry

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.my_wallets.create_wallet.CreateWalletDialogFragment
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

  private val viewModel: RecoverWalletViewModel by viewModels()
  private val views by viewBinding(RecoverEntryFragmentBinding::bind)

  lateinit var requestPermissionsLauncher: ActivityResultLauncher<String>
  lateinit var storageIntentLauncher: ActivityResultLauncher<Intent>

  override fun onCreateView(
    inflater: LayoutInflater, @Nullable container: ViewGroup?,
    @Nullable savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.recover_entry_fragment, container, false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    createLaunchers()
    handleFragmentResult()
  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.recoverWalletOptions.recoverFromFileButton.setOnClickListener {
      requestPermissionsLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    views.recoverWalletButton.setOnClickListener {
      viewModel.handleRecoverClick(
        views.recoverWalletOptions.keystoreEditText.text.toString()
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
    Log.d(
      "APPC-2780",
      "RecoverWalletFragment: handleWalletModelRecoverState: state -> $asyncRecoverResult "
    )
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
    Log.d("APPC-2780", "RecoverEntryFragment: handleSuccessState: $recoverResult ")
    when (recoverResult) {
      is SuccessfulEntryRecover -> {
        navigator.navigateToCreateWalletDialog()
      }
      else -> handleErrorState(recoverResult)
    }
  }

  private fun handleErrorState(recoverResult: RecoverEntryResult) {
    Log.d("APPC-2780", "RecoverEntryFragment: handleErrorState: $recoverResult ")
    views.recoverWalletOptions.labelInput.isErrorEnabled = true
    when (recoverResult) {
      is FailedEntryRecover.AlreadyAdded -> {
        views.recoverWalletOptions.labelInput.error = getString(R.string.error_already_added)
      }
      is FailedEntryRecover.InvalidKeystore -> {
        views.recoverWalletOptions.labelInput.error = getString(R.string.error_import)
      }
      is FailedEntryRecover.InvalidPassword -> {
        navigator.navigateToRecoverPasswordFragment(
          keystore = recoverResult.key,
          walletBalance = recoverResult.symbol + recoverResult.amount,
          walletAddress = recoverResult.address
        )
        viewModel.resetState()
      }
      is FailedEntryRecover.InvalidPrivateKey -> {
        views.recoverWalletOptions.labelInput.error = getString(R.string.error_import)
      }
      is FailedEntryRecover.GenericError -> {
        views.recoverWalletOptions.labelInput.error = getString(R.string.error_general)
      }
      else -> return
    }
  }

  private fun handleFragmentResult() {
    parentFragmentManager.setFragmentResultListener(
      CreateWalletDialogFragment.RESULT_REQUEST_KEY,
      this
    ) { _, _ ->
      activity?.finish()
//      navigator.navigateToMainActivity(fromSupportNotification = false)
    }
  }

  companion object {
    fun newInstance() = RecoverEntryFragment()
  }
}
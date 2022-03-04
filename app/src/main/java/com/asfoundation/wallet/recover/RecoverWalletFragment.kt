package com.asfoundation.wallet.recover

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
import com.asf.wallet.databinding.FragmentRecoverWalletBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RecoverWalletFragment : BasePageViewFragment(),
  SingleStateFragment<RecoverWalletState, RecoverWalletSideEffect> {

  @Inject
  lateinit var recoverWalletNavigator: RecoverWalletNavigator

  private val viewModel: RecoverWalletViewModel by viewModels()
  private val views by viewBinding(FragmentRecoverWalletBinding::bind)

  lateinit var requestPermissionsLauncher: ActivityResultLauncher<String>
  lateinit var storageIntentLauncher: ActivityResultLauncher<Intent>

  override fun onCreateView(
    inflater: LayoutInflater, @Nullable container: ViewGroup?,
    @Nullable savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_recover_wallet, container, false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    createLaunchers()
  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.recoverWalletOptions.recoverFromFileButton.setOnClickListener {
      requestPermissionsLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    views.recoverWalletButton.setOnClickListener {
      viewModel.handleRecoverClick(
        views.recoverWalletOptions.keystoreEditText.text.toString(),
        passwordRequired = false
      )
    }
//    views.recoverWalletPasswordButton.setOnClickListener {
//      viewModel.handleRecoverClick("", passwordRequired = true)
//    }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  fun createLaunchers() {
    requestPermissionsLauncher =
      registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
          recoverWalletNavigator.launchFileIntent(storageIntentLauncher, viewModel.filePath())
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

  override fun onStateChanged(state: RecoverWalletState) {
    handleWalletModelRecoverState(state.recoverResultAsync)
  }

  override fun onSideEffect(sideEffect: RecoverWalletSideEffect) = Unit

  private fun handleWalletModelRecoverState(asyncRecoverResult: Async<RecoverWalletResult>) {
    Log.d(
      "APPC-2780",
      "RecoverWalletFragment: handleWalletModelRecoverState: state -> $asyncRecoverResult "
    )
    when (asyncRecoverResult) {
      is Async.Uninitialized -> {
        showRecoverOptionsScreen()
      }
      is Async.Loading -> {
        showLoading()
      }
      is Async.Fail -> {
        handleErrorState(FailedWalletRecover.GenericError(asyncRecoverResult.error.throwable))
      }
      is Async.Success -> {
        handleSuccessState(asyncRecoverResult())
      }
    }
  }

  fun showRecoverOptionsScreen() {
    views.recoverWalletOptions.root.visibility = View.VISIBLE
    views.recoverWalletPassword.root.visibility = View.GONE
    views.recoverWalletButton.visibility = View.VISIBLE
    views.recoverWalletPasswordButton.visibility = View.GONE
  }

  fun showRecoverPasswordScreen() {
    views.recoverWalletOptions.root.visibility = View.GONE
    views.recoverWalletPassword.root.visibility = View.VISIBLE
    views.recoverWalletButton.visibility = View.GONE
    views.recoverWalletPasswordButton.visibility = View.VISIBLE
  }

  fun showLoading() {

  }

  private fun handleSuccessState(recoverResult: RecoverWalletResult) {
    Log.d("APPC-2780", "RecoverWalletFragment: handleSuccessState: $recoverResult ")
    when (recoverResult) {
      is SuccessfulWalletRecover -> {

      }
      else -> handleErrorState(recoverResult)
    }
  }

  private fun handleErrorState(recoverResult: RecoverWalletResult) {
    Log.d("APPC-2780", "RecoverWalletFragment: handleErrorState: $recoverResult ")
    views.recoverWalletOptions.labelInput.isErrorEnabled = true
    when (recoverResult) {
      is FailedWalletRecover.AlreadyAdded -> {
        views.recoverWalletOptions.labelInput.error = getString(R.string.error_already_added)
      }
      is FailedWalletRecover.InvalidKeystore -> {
        views.recoverWalletOptions.labelInput.error = getString(R.string.error_import)
      }
      is FailedWalletRecover.InvalidPassword -> {

      }
      is FailedWalletRecover.RequirePassword -> {
        views.recoverWalletPassword.walletBalance.text = recoverResult.symbol + recoverResult.amount
        views.recoverWalletPassword.walletAddress.text = recoverResult.address
        showRecoverPasswordScreen()
      }
      is FailedWalletRecover.InvalidPrivateKey -> {
        views.recoverWalletOptions.labelInput.error = getString(R.string.error_import)
      }
      is FailedWalletRecover.GenericError -> {
        views.recoverWalletOptions.labelInput.error = getString(R.string.error_general)
      }
      else -> return
    }
  }

  companion object {
    fun newInstance() = RecoverWalletFragment()
  }
}
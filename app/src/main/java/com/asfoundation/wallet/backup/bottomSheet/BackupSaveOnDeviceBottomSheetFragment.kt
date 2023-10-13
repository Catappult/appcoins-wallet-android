package com.asfoundation.wallet.backup.bottomSheet

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.feature.backup.ui.save_on_device.BackupSaveOnDeviceDialogState
import com.appcoins.wallet.feature.backup.ui.save_on_device.BackupSaveOnDeviceSideEffect
import com.appcoins.wallet.feature.backup.ui.save_on_device.BackupSaveOnDeviceViewModel
import com.asf.wallet.R
import com.asf.wallet.databinding.BackupSaveOnDeviceDialogFragmentBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BackupSaveOnDeviceBottomSheetFragment : BottomSheetDialogFragment(),
  SingleStateFragment<BackupSaveOnDeviceDialogState, BackupSaveOnDeviceSideEffect> {

  @Inject
  lateinit var navigator: BackupSaveOnDeviceBottomSheetNavigator

  private lateinit var requestPermissionsLauncher: ActivityResultLauncher<String>
  private lateinit var openDocumentTreeResultLauncher: ActivityResultLauncher<Intent>


  private val viewModel: BackupSaveOnDeviceViewModel by viewModels()
  private val views by viewBinding(BackupSaveOnDeviceDialogFragmentBinding::bind)

  companion object {
    const val WALLET_ADDRESS_KEY = "wallet_address"
    const val PASSWORD_KEY = "password"
    private const val FILE_NAME_EXTRA_KEY = "file_name"

    @JvmStatic
    fun newInstance(walletAddress: String, password: String) =
      BackupSaveOnDeviceBottomSheetFragment()
        .apply {
          arguments = Bundle().apply {
            putString(WALLET_ADDRESS_KEY, walletAddress)
            putString(PASSWORD_KEY, password)
          }
        }
  }
  private fun createLaunchers() {
    openDocumentTreeResultLauncher = registerForActivityResult(
      ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
      val data = activityResult.data
      if (activityResult.resultCode == Activity.RESULT_OK && data != null) {
        data.data?.let {
          val documentFile = DocumentFile.fromTreeUri(requireContext(), it)
          lifecycleScope.launch {
               viewModel.saveBackupFile(views.fileNameInput.getText(), documentFile)
          }
        }
      }
    }
    requestPermissionsLauncher = registerForActivityResult(
      ActivityResultContracts.RequestPermission()
    ) { isGranted ->
      if (isGranted) {
        lifecycleScope.launch {
             viewModel.saveBackupFile(views.fileNameInput.getText())
        }
      }
    }
  }

  @SuppressLint("ResourceAsColor")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.backupSave.setOnClickListener {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
          putExtra(FILE_NAME_EXTRA_KEY, views.fileNameInput.getText())
        }
        openDocumentTreeResultLauncher.launch(intent)
      } else {
        requestPermissionsLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
      }
    }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    createLaunchers()
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = BackupSaveOnDeviceDialogFragmentBinding.inflate(inflater).root

  private fun setFileName(fileName: String) = views.fileNameInput.setText(fileName)

  private fun setFilePath(downloadsPath: String?) {
    if (downloadsPath != null) {
      views.storePath.text = downloadsPath
      views.storePath.visibility = View.VISIBLE
    } else {
      views.storePath.visibility = View.GONE
    }
  }

  override fun onStateChanged(state: BackupSaveOnDeviceDialogState) {
    if(views.fileNameInput.getText().isEmpty()) {
      state.fileName()?.also { setFileName(it) }
    }
      setFilePath(state.downloadsPath)
  }



  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun onSideEffect(sideEffect: BackupSaveOnDeviceSideEffect) {
    when (sideEffect) {
      BackupSaveOnDeviceSideEffect.NavigateToSuccess -> navigator.navigateToSuccessScreen(
        navController()
      )

      else -> {}
    }
  }

  override fun getTheme(): Int {
    return R.style.AppBottomSheetDialogThemeDraggable
  }

  fun showError() {
    Toast.makeText(
      context,
      com.appcoins.wallet.feature.backup.ui.R.string.error_export,
      Toast.LENGTH_LONG
    ).show()
    requireActivity().finish()
  }

  private fun navController(): NavController {
    val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(
      R.id.main_host_container
    ) as NavHostFragment
    return navHostFragment.navController
  }

}


package com.asfoundation.wallet.backup.save_on_device

import android.Manifest
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
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.BackupSaveOnDeviceDialogFragmentBinding
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupSaveOnDeviceDialogFragment : BottomSheetDialogFragment(),
  SingleStateFragment<BackupSaveOnDeviceDialogState, BackupSaveOnDeviceDialogSideEffect> {

  @Inject
  lateinit var navigator: BackupSaveOnDeviceDialogNavigator

  private lateinit var requestPermissionsLauncher: ActivityResultLauncher<String>
  private lateinit var openDocumentTreeResultLauncher: ActivityResultLauncher<Intent>

  private val viewModel: BackupSaveOnDeviceDialogViewModel by viewModels()
  private val views by viewBinding(BackupSaveOnDeviceDialogFragmentBinding::bind)

  companion object {
    const val WALLET_ADDRESS_KEY = "wallet_address"
    const val PASSWORD_KEY = "password"
    private const val FILE_NAME_EXTRA_KEY = "file_name"

    @JvmStatic
    fun newInstance(walletAddress: String, password: String) =
      BackupSaveOnDeviceDialogFragment()
        .apply {
          arguments = Bundle().apply {
            putString(WALLET_ADDRESS_KEY, walletAddress)
            putString(PASSWORD_KEY, password)
          }
        }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    createLaunchers()
  }

  private fun createLaunchers() {
    openDocumentTreeResultLauncher = registerForActivityResult(
      ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
      val data = activityResult.data
      if (activityResult.resultCode == Activity.RESULT_OK && data != null) {
        data.data?.let {
          val documentFile = DocumentFile.fromTreeUri(requireContext(), it)
          viewModel.saveBackupFile(views.fileNameInput.getText(), documentFile)
        }
      }
    }
    requestPermissionsLauncher = registerForActivityResult(
      ActivityResultContracts.RequestPermission()
    ) { isGranted ->
      if (isGranted) {
        viewModel.saveBackupFile(views.fileNameInput.getText())
      }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = BackupSaveOnDeviceDialogFragmentBinding.inflate(layoutInflater).root

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
    views.backupCancel.setOnClickListener {
      navigator.navigateBack()
    }

    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int = R.style.AppBottomSheetDialogThemeDraggable

  override fun onStateChanged(state: BackupSaveOnDeviceDialogState) {
    state.fileName()?.also { setFileName(it) }
    setFilePath(state.downloadsPath)
  }

  private fun setFileName(fileName: String) = views.fileNameInput.setText(fileName)

  private fun setFilePath(downloadsPath: String?) {
    if (downloadsPath != null) {
      views.storePath.text = downloadsPath
      views.storePath.visibility = View.VISIBLE
    } else {
      views.storePath.visibility = View.GONE
    }
  }

  override fun onSideEffect(sideEffect: BackupSaveOnDeviceDialogSideEffect) =
    when (sideEffect) {
      is BackupSaveOnDeviceDialogSideEffect.NavigateToSuccess ->
        navigator.navigateToSuccessScreen(sideEffect.walletAddress)
      BackupSaveOnDeviceDialogSideEffect.ShowError -> showError()
    }

  fun showError() {
    Toast.makeText(context, R.string.error_export, Toast.LENGTH_LONG).show()
    requireActivity().finish()
  }
}
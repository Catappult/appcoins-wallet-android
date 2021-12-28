package com.asfoundation.wallet.ui.backup.save

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.SaveBackupLayoutBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.di.DaggerBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import javax.inject.Inject

class SaveBackupBottomSheetFragment : DaggerBottomSheetDialogFragment(),
    SingleStateFragment<SaveBackupBottomSheetState, SaveBackupBottomSheetSideEffect> {


  @Inject
  lateinit var saveBackupBottomSheetViewModelFactory: SaveBackupBottomSheetViewModelFactory

  @Inject
  lateinit var navigator: SaveBackupBottomSheetNavigator

  private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

  private val viewModel: SaveBackupBottomSheetViewModel by viewModels { saveBackupBottomSheetViewModelFactory }
  private val views by viewBinding(SaveBackupLayoutBinding::bind)

  companion object {
    const val WALLET_ADDRESS_KEY = "wallet_address"
    const val PASSWORD_KEY = "password"
    private const val FILE_NAME_EXTRA_KEY = "file_name"

    @JvmStatic
    fun newInstance(walletAddress: String, password: String): SaveBackupBottomSheetFragment {
      return SaveBackupBottomSheetFragment()
          .apply {
            arguments = Bundle().apply {
              putString(WALLET_ADDRESS_KEY, walletAddress)
              putString(PASSWORD_KEY, password)
            }
          }
    }

  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { activityResult ->
      val data = activityResult.data
      if (activityResult.resultCode == Activity.RESULT_OK && data != null) {
        data.data?.let {
          val documentFile = DocumentFile.fromTreeUri(requireContext(), it)
          viewModel.saveBackupFile(views.fileNameInput.getText(), documentFile)
        }
      }
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.save_backup_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.backupSave.setOnClickListener {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
          putExtra(FILE_NAME_EXTRA_KEY, views.fileNameInput.getText())
        }
        activityResultLauncher.launch(intent)
      } else {
        viewModel.saveBackupFile(views.fileNameInput.getText())
        navigator.navigateSuccessScreen()
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

  override fun getTheme(): Int {
    return R.style.AppBottomSheetDialogThemeNoFloating
  }

  override fun onStateChanged(state: SaveBackupBottomSheetState) {
    setFileName(state.fileName)
    setFilePath(state.downloadsPath)
  }

  private fun setFileName(fileName: String) {
    views.fileNameInput.setText(fileName)
  }

  private fun setFilePath(downloadsPath: String?) {
    if (downloadsPath != null) {
      views.storePath.text = downloadsPath
      views.storePath.visibility = View.VISIBLE
    } else {
      views.storePath.visibility = View.GONE
    }
  }

  override fun onSideEffect(sideEffect: SaveBackupBottomSheetSideEffect) = when (sideEffect) {
    SaveBackupBottomSheetSideEffect.NavigateToSuccess -> navigator.navigateSuccessScreen()
  }
}
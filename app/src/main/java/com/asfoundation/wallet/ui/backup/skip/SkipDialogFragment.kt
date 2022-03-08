package com.asfoundation.wallet.ui.backup.skip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.BackupSkipLayoutBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class SkipDialogFragment : BottomSheetDialogFragment(),
    SingleStateFragment<SkipDialogState, SkipDialogSideEffect> {


  @Inject
  lateinit var skipDialogViewModelFactory: SkipDialogViewModelFactory

  @Inject
  lateinit var navigator: SkipDialogNavigator

  private val viewModel: SkipDialogViewModel by viewModels { skipDialogViewModelFactory }
  private val views by viewBinding(BackupSkipLayoutBinding::bind)

  companion object {
    @JvmStatic
    fun newInstance(): SkipDialogFragment {
      return SkipDialogFragment()
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.backup_skip_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.confirm.setOnClickListener {
      navigator.finishBackup()
    }
    views.cancel.setOnClickListener {
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

  override fun onStateChanged(state: SkipDialogState) = Unit


  override fun onSideEffect(sideEffect: SkipDialogSideEffect) = Unit
}
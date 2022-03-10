package com.asfoundation.wallet.ui.backup.skip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.BackupSkipLayoutBinding
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.base.ViewState
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class BackupSkipDialogFragment : BottomSheetDialogFragment(),
  SingleStateFragment<ViewState, SideEffect> {


  @Inject
  lateinit var navigator: BackupSkipDialogNavigator

  private val views by viewBinding(BackupSkipLayoutBinding::bind)

  companion object {
    @JvmStatic
    fun newInstance(): BackupSkipDialogFragment {
      return BackupSkipDialogFragment()
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
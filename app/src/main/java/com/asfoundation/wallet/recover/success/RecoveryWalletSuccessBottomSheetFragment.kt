package com.asfoundation.wallet.recover.success

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.utils.android_common.AppUtils
import com.asf.wallet.R
import com.asf.wallet.databinding.RecoveryWalletSuccessBottomSheetLayoutBinding
import com.asfoundation.wallet.recover.entry.RecoverEntryNavigator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RecoveryWalletSuccessBottomSheetFragment : BottomSheetDialogFragment(),
  SingleStateFragment<ViewState, SideEffect> {


  @Inject
  lateinit var navigator: RecoverEntryNavigator

  private val views by viewBinding(RecoveryWalletSuccessBottomSheetLayoutBinding::bind)

  companion object {

    const val IS_FROM_ONBOARDING = "is_from_onboarding"

    @JvmStatic
    fun newInstance(isFromOnboarding: Boolean): RecoveryWalletSuccessBottomSheetFragment {
      return RecoveryWalletSuccessBottomSheetFragment()
        .apply {
          arguments = Bundle().apply {
            putBoolean(IS_FROM_ONBOARDING, isFromOnboarding)
          }
        }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = RecoveryWalletSuccessBottomSheetLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    views.recoveryWalletBottomButton.setOnClickListener {
      if (requireArguments().getBoolean(IS_FROM_ONBOARDING)) {
        restart()
      } else {
        navigator.navigateBack()
        dismiss()
      }
    }
  }

  private fun restart() {
    lifecycleScope.launch {
      AppUtils.restartApp(
        activity = requireActivity(),
        copyIntent = true
      )
    }
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(views.root.parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int {
    return R.style.AppBottomSheetDialogThemeDraggable
  }

  override fun onStateChanged(state: ViewState) = Unit

  override fun onSideEffect(sideEffect: SideEffect) = Unit

}
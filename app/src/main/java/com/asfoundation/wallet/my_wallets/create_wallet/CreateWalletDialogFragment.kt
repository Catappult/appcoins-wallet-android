package com.asfoundation.wallet.my_wallets.create_wallet

import android.animation.Animator
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentCreateWalletDialogLayoutBinding
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.SingleStateFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CreateWalletDialogFragment : DialogFragment(),
  SingleStateFragment<CreateWalletState, CreateWalletSideEffect> {

  @Inject
  lateinit var navigator: CreateWalletDialogNavigator

  private val viewModel: CreateWalletDialogViewModel by viewModels()
  private val views by viewBinding(FragmentCreateWalletDialogLayoutBinding::bind)

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
    object : Dialog(requireContext(), theme) {
      override fun onBackPressed() {
        // Do nothing
      }
    }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = FragmentCreateWalletDialogLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    //Temporary solution until this animation is refactored to the new design
    if (requireArguments().getBoolean(NEEDS_WALLET_CREATION)) {
      views.createWalletText.text = getText(R.string.provide_wallet_creating_wallet_header)
      viewModel.createNewWallet(requireArguments().getBoolean(IS_FROM_ONBOARDING))
    }
  }

  override fun onDestroy() {
    setFragmentResult(CREATE_WALLET_DIALOG_COMPLETE, bundleOf("fragmentEnded" to "result"))
    super.onDestroy()
  }

  override fun getTheme(): Int = R.style.FullScreenDialogStyle

  override fun onStateChanged(state: CreateWalletState) {
    when (state.walletCreationAsync) {
      Async.Uninitialized,
      is Async.Loading -> {
        views.createWalletLoading.playAnimation()
      }
      is Async.Success -> {
        views.createWalletLoading.setAnimation(R.raw.success_animation)
        if (requireArguments().getBoolean(NEEDS_WALLET_CREATION)) {
          views.createWalletText.text = getText(R.string.provide_wallet_created_header)
        } else {
          views.createWalletText.text = getText(R.string.wallets_imported_body)
        }

        if (requireArguments().getBoolean(IS_FROM_ONBOARDING)) {
          navigator.navigateBack()
        } else {
          views.createWalletLoading.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) = Unit
            override fun onAnimationEnd(animation: Animator) = navigator.navigateBack()
            override fun onAnimationCancel(animation: Animator) = Unit
            override fun onAnimationStart(animation: Animator) = Unit
          })
        }
        views.createWalletLoading.repeatCount = 0
        views.createWalletLoading.playAnimation()
      }
      else -> Unit
    }
  }

  override fun onSideEffect(sideEffect: CreateWalletSideEffect) = Unit

  companion object {
    const val CREATE_WALLET_DIALOG_COMPLETE = "create_wallet_dialog_complete"
    const val NEEDS_WALLET_CREATION = "needs_wallet_creation"
    const val IS_FROM_ONBOARDING = "is_from_onboarding"
  }
}